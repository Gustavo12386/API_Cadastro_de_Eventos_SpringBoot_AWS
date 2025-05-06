package com.eventostec.api.application.service;

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

import com.eventostec.api.adapters.outbound.repositories.JpaEventRepository;
import com.eventostec.api.adapters.outbound.storage.ImageUploaderPort;
import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventAddressProjection;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRepository;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.utils.mappers.EventMapper;

@Service
public class EventServiceImpl {
	
	  @Value("${admin.key}")
	  private String adminKey;
	  
	  @Autowired
	  private ImageUploaderPort imageUploaderPort;
	  
	  @Autowired
	  private EventRepository repository;	  
	  
	  @Autowired
	  private AddressService addressService;

	  @Autowired
	  private CouponService couponService;
	  
	  @Autowired
	  private EventMapper mapper;
		
	  public Event createEvent(EventRequestDTO data) {
		    String imgUrl = null;

		    if (data.image() != null) {
		        imgUrl = imageUploaderPort.uploadImage(data.image());
		    }
		   
		    Event newEvent = mapper.dtoToEntity(data, imgUrl);
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
		  Page<EventAddressProjection> eventsPage = this.repository.findUpcomingEvents(page, size);
		  return eventsPage.map(event -> new EventResponseDTO(
				  event.getId(),
				  event.getTitle(),
				  event.getDescription(),
				  event.getDate(),
				  event.getCity() != null ? event.getCity() : "",
				  event.getUf() != null ? event.getUf() : "",
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
		 
		  Page<EventAddressProjection> eventsPage = this.repository.findFilteredEvents(city, uf, startDate,
		  endDate,page, size);
		  
		  return eventsPage.map(event -> new EventResponseDTO(
		          event.getId(),
		          event.getTitle(),
		          event.getDescription(),
		          event.getDate(),
		          event.getCity() != null ? event.getCity() : "",
				  event.getUf() != null ? event.getUf() : "",
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
	      
	      return mapper.toDetailsDTO(event, address, coupons);

	  }
	  
	  public void deleteEvent(UUID eventId, String adminKey){
	        if(adminKey == null || !adminKey.equals(this.adminKey)){
	            throw new IllegalArgumentException("Invalid admin key");
	        }

	        this.repository.deleteById(eventId);

	  }
	  
	  public List<EventResponseDTO> searchEvents(String title){
	        title = (title != null) ? title : "";

	        List<EventAddressProjection> eventsList = this.repository.findEventsByTitle(title);
	        return eventsList.stream().map(event -> new EventResponseDTO(
	                        event.getId(),
	                        event.getTitle(),
	                        event.getDescription(),
	                        event.getDate(),
	                        event.getCity() != null ? event.getCity() : "",
	                        event.getUf() != null ? event.getUf() : "",
	                        event.getRemote(),
	                        event.getEventUrl(),
	                        event.getImgUrl())
	                )
	                .toList();
	    }
	  
	
	  private File covertMultipartToFile(MultipartFile multipartFile ) throws IOException {
		  File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
		  FileOutputStream fos = new FileOutputStream(convFile); 
		  fos.write(multipartFile.getBytes());
		  fos.close();
		  return convFile;
	  }
}
