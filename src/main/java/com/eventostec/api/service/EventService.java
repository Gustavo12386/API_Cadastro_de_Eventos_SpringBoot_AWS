package com.eventostec.api.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {
	
  @Value("${aws.bucket.name}")
  private String bucketName;	
  
  @Autowired
  private EventRepository repository;
  
  @Autowired
  private AmazonS3 s3Client;
  
  @Autowired
  private AddressService addressService;

  @Autowired
  private CouponService couponService;
	
  public Event createEvent(EventRequestDTO data) {
	    String imgUrl = null;

	    if (data.image() != null) {
	        imgUrl = this.uploadImg(data.image());
	    }
	   
	    Event newEvent = new Event();
	    newEvent.setTitle(data.title());
	    newEvent.setDescription(data.description());
	    newEvent.setEventUrl(data.eventUrl());
	    newEvent.setDate(new Date(data.date()));
	    newEvent.setImgUrl(imgUrl);
	    newEvent.setRemote(data.remote());
	    
	    repository.save(newEvent);
        
	    if(!data.remote()) {
	    	this.addressService.createAddress(data, newEvent);
	    }

	    return newEvent;
	}
  
  public List<EventResponseDTO> getUpcomingEvents(int page, int size){
	  Pageable pageable = PageRequest.of(page, size);
	  Page<Event> eventsPage = this.repository.findUpcomingEvents(new Date(), pageable);
	  return eventsPage.map(event -> new EventResponseDTO(
			  event.getId(),
			  event.getTitle(),
			  event.getDescription(),
			  event.getDate(),
			  event.getAddress() != null ? event.getAddress().getCity() : "",
			  event.getAddress() != null ? event.getAddress().getUf() : "",
			  event.getRemote(),
			  event.getEventUrl(),
			  event.getImgUrl())
			  )
			  .stream().toList();
	  
  }
  
  public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf,
	      Date startDate, Date endDate){
	  
	  city = (city != null) ? city : "";
	  uf = (uf != null) ? uf : "";
	  startDate = (startDate != null) ? startDate : new Date(0);
	  endDate = (endDate != null) ? endDate : new Date();
	  
	  startDate = (startDate != null) ? startDate : new Date(0);
	  endDate = (endDate != null) ? endDate : new Date();
	  
	  Pageable pageable = PageRequest.of(page, size);
	  Page<Event> eventsPage = this.repository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);
	  
	  return eventsPage.map(event -> new EventResponseDTO(
	          event.getId(),
	          event.getTitle(),
	          event.getDescription(),
	          event.getDate(),
	          event.getAddress() != null ? event.getAddress().getCity() : "",
	          event.getAddress() != null ? event.getAddress().getUf() : "",
	          event.getRemote(),
	          event.getEventUrl(),
	          event.getImgUrl())
	          )
	          .stream().toList();
	}
  
  public EventDetailsDTO getEventDetails(UUID eventId) {
      Event event = repository.findById(eventId)
              .orElseThrow(() -> new IllegalArgumentException("Event not found"));

      Optional<Address> address = addressService.findByEventId(eventId);

      List<Coupon> coupons = couponService.consultCoupons(eventId, new Date());

      List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream()
              .map(coupon -> new EventDetailsDTO.CouponDTO(
                      coupon.getCode(),
                      coupon.getDiscount(),
                      coupon.getValid()))
              .collect(Collectors.toList());

      return new EventDetailsDTO(
              event.getId(),
              event.getTitle(),
              event.getDescription(),
              event.getDate(),
              address.isPresent() ? address.get().getCity() : "",
              address.isPresent() ? address.get().getUf() : "",
              event.getImgUrl(),
              event.getEventUrl(),
              couponDTOs);
  }
  
  private String uploadImg(MultipartFile multipartFile) {
	  String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
	  try {
		File file = this.covertMultipartToFile(multipartFile);		
		s3Client.putObject(bucketName, filename, file);
		file.delete();
		return s3Client.getUrl(bucketName, filename).toString();
	  }catch(Exception e){
		 System.out.println("erro ao subir arquivo"); 
		 return "";
	  }	 
  }
  
  private File covertMultipartToFile(MultipartFile multipartFile ) throws IOException {
	  File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
	  FileOutputStream fos = new FileOutputStream(convFile); 
	  fos.write(multipartFile.getBytes());
	  fos.close();
	  return convFile;
  }
}
