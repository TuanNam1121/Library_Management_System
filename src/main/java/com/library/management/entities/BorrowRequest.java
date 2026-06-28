package com.library.management.entities;

import com.library.management.enums.BorrowStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="borrow_requests")
@Getter
@Setter
public class BorrowRequest extends BaseEntity{


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name="reader_id")
    private User reader;



    @ManyToOne
    @JoinColumn(name="approved_by")
    private User librarian;



    @Enumerated(EnumType.STRING)
    private BorrowStatus status;

    private LocalDateTime requestDate;

    private LocalDateTime approvedAt;

    @OneToMany(

            mappedBy = "borrowRequest",

            cascade = CascadeType.ALL
    )
    private List<BorrowDetail> details;


}
