package com.chsbk.boilers_web.repository;

import com.chsbk.boilers_web.entities.Event;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends PagingAndSortingRepository<Event, Long> {
}
