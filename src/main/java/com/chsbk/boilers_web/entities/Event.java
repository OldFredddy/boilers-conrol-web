package com.chsbk.boilers_web.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter @Setter
@Immutable
public class Event {

    /** В продакшне id действительно уникален, так что можно обойтись без composite key */
    @Id
    private Long id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "evt_type")
    private String evtType;

    private String action;

    /** jsonb хранится просто как строка – для просмотра этого достаточно */
    @Column(columnDefinition = "jsonb")
    private String payload;

    private int status;

    private LocalDateTime ts;
}
