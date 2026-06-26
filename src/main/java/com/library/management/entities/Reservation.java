package com.library.management.entities;

import com.library.management.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="reservations")
@Getter
@Setter
public class Reservation extends BaseEntity{


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name="reader_id")
    private User reader;



    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;



    private LocalDateTime reservedAt;



    private LocalDateTime expireAt;



    private LocalDateTime pickedUpAt;



    @Enumerated(EnumType.STRING)
    private ReservationStatus status;


}
