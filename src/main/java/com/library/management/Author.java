package com.library.management;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "Authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AuthorId", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "AuthorName", nullable = false, length = 100)
    private String authorName;

    @Nationalized
    @Lob
    @Column(name = "Biography")
    private String biography;

}