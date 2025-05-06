package com.chsbk.boilers_web.entities;


import jakarta.persistence.*;
import lombok.Data;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    private String evtType;
    private String action;
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String payload;

    private int status;
    private LocalDateTime ts = LocalDateTime.now();
}
