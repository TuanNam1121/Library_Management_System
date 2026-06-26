package com.library.management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="authors")
@Getter
@Setter
public class Author extends BaseEntity{


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    private String name;


    @Column(columnDefinition = "TEXT")
    private String bio;


}
