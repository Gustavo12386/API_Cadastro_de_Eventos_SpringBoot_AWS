package com.eventostec.api.domain.address;

import java.util.UUID;

import com.eventostec.api.domain.event.Event;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "address")
public class Address {
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO) 
	private UUID id;
    private String city;
    private String uf;
    
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    
    
}
