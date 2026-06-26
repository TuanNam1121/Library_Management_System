package com.library.management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookId", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "ISBN", length = 30)
    private String isbn;

    @Nationalized
    @Lob
    @Column(name = "Description")
    private String description;

    @Column(name = "PublishYear")
    private Integer publishYear;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "AvailableQuantity", nullable = false)
    private Integer availableQuantity;

    @Nationalized
    @Column(name = "CoverImage")
    private String coverImage;

    @ColumnDefault("0")
    @Column(name = "Status", nullable = false)
    private Boolean status = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AuthorId", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    @ColumnDefault("getdate()")
    @Column(name = "CreatedAt")
    private Instant createdAt;

}