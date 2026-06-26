package com.library.management.entities;

import com.library.management.enums.BorrowItemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name="borrow_details")
@Getter
@Setter
public class BorrowDetail {


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name="borrow_request_id")
    private BorrowRequest borrowRequest;



    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;



    private LocalDate borrowDate;



    private LocalDate dueDate;



    private LocalDate returnDate;



    @Enumerated(EnumType.STRING)
    private BorrowItemStatus status;



    @OneToOne(mappedBy = "borrowDetail",
            cascade = CascadeType.ALL)
    private Fine fine;


}
