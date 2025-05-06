package com.chsbk.boilers_web.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String source;
    private String ip;
    private String userAgent;
    private LocalDateTime startTs = LocalDateTime.now();
    private LocalDateTime endTs;
}
