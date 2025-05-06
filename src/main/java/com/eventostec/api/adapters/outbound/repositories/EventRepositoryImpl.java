package com.eventostec.api.adapters.outbound.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.eventostec.api.adapters.outbound.entities.JpaEventEntity;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventAddressProjection;
import com.eventostec.api.domain.event.EventRepository;
import com.eventostec.api.utils.mappers.EventMapper;

@Repository
public class EventRepositoryImpl implements EventRepository {
	
	@Autowired
	private JpaEventRepository eventRepository;	
	
	@Autowired
	private EventMapper mapper;
	
	public EventRepositoryImpl(JpaEventRepository eventRepository) {
		this.eventRepository = eventRepository;		
	}
	
	@Override
	public Event save(Event event) {
		JpaEventEntity eventEntity = new JpaEventEntity(event);
		this.eventRepository.save(eventEntity);
		return new Event(eventEntity.getId(), eventEntity.getTitle(), eventEntity.getDescription(), eventEntity.getImgUrl(), 
		eventEntity.getEventUrl(), eventEntity.getRemote(), eventEntity.getDate());
	}

	@Override
	public Optional<Event> findById(UUID id) {
		Optional<JpaEventEntity> eventEntity = this.eventRepository.findById(id);
		return eventEntity.map(mapper::jpaToDomain);
	}

	@Override
	public List<Event> findAll() {
		return this.eventRepository.findAll()
		.stream().map(entity -> new Event(entity.getId(), entity.getTitle(), entity.getDescription(),
		entity.getImgUrl(), entity.getEventUrl(), entity.getRemote(), entity.getDate())).collect(Collectors.toList());
		
	}

	@Override
	public void deleteById(UUID id) {
		this.eventRepository.deleteById(id);
		
	}

	@Override
	public Page<EventAddressProjection> findUpcomingEvents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return this.eventRepository.findUpcomingEvents(new Date(), pageable);
	}

	@Override
	public Page<EventAddressProjection> findFilteredEvents(String city, String uf, Date startDate, Date endDate,
		int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return this.eventRepository.findFilteredEvents(uf, city, uf, startDate, endDate, pageable);
	}

	@Override
	public List<EventAddressProjection> findEventsByTitle(String title) {
		return this.eventRepository.findEventsByTitle(title);
	}
}
