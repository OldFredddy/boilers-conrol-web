package com.chsbk.boilers_web.services;

import com.chsbk.boilers_web.entities.EventDTO;
import com.chsbk.boilers_web.entities.EventMapper;
import com.chsbk.boilers_web.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    private final EventRepository repo;
    private final EventMapper mapper;
@Autowired
    public EventService(EventRepository repo, EventMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<EventDTO> lastEvents(int limit) {
        var page = PageRequest.of(0, limit, Sort.Direction.DESC, "ts");
        return repo.findAllByOrderByTsDesc(page).stream()
                .map(mapper::toDto)
                .toList();
    }
}
