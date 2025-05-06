package com.chsbk.boilers_web.repository;

import com.chsbk.boilers_web.entities.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    // последние N по времени
    List<Event> findAllByOrderByTsDesc(Pageable pageable);
}
