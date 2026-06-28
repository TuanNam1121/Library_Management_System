package com.library.management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="notifications")
@Getter
@Setter
public class Notification extends BaseEntity{


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean readStatus = false;
}
