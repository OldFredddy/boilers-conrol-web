package com.chsbk.boilers_web.services;

import com.chsbk.boilers_web.entities.Event;
import com.chsbk.boilers_web.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repo;

    public Page<Event> getPage(int page, int size) {

        return repo.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ts"))
        );
    }
}
