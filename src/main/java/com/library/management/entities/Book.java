package com.library.management.entities;

import com.library.management.enums.BookStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="books")
@Getter
@Setter
public class Book extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String isbn;

    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer quantity;

    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name="author_id")
    private Author author;

    @ManyToOne
    @JoinColumn(name="approved_by")
    private User approvedBy;

    private LocalDateTime approvedAt;

}