package com.eventostec.api.adapters.outbound.repositories;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventostec.api.adapters.outbound.entities.JpaEventEntity;
import com.eventostec.api.domain.event.EventAddressProjection;

public interface JpaEventRepository extends JpaRepository<JpaEventEntity, UUID> {
   
	@Query("SELECT e FROM Event e LEFT JOIN FETCH e.address a WHERE e.date >= :currentDate")
	public Page<EventAddressProjection> findUpcomingEvents(@Param("currentDate") Date currentDate, Pageable pageable);

	@Query("SELECT e FROM Event e LEFT JOIN e.address a " +
		       "WHERE (:title = '' OR e.title LIKE %:title%) " +
		       "AND (:city = '' OR a.city LIKE %:city%) " +
		       "AND (:uf = '' OR a.uf LIKE %:uf%) " +
		       "AND (e.date >= :startDate AND e.date <= :endDate)")
		Page<EventAddressProjection> findFilteredEvents(
		    @Param("title") String title,
		    @Param("city") String city,
		    @Param("uf") String uf,
		    @Param("startDate") Date startDate,
		    @Param("endDate") Date endDate,
		    Pageable pageable
		);
	
	  @Query("SELECT e.id AS id, e.title AS title, e.description AS description, e.date AS date, e.imgUrl AS imgUrl, e.eventUrl AS eventUrl, e.remote AS remote, a.city AS city, a.uf AS uf " +
	            "FROM Event e JOIN Address a ON e.id = a.event.id " +
	            "WHERE (:title = '' OR e.title LIKE %:title%)")
	    List<EventAddressProjection> findEventsByTitle(@Param("title") String title);
	

}
