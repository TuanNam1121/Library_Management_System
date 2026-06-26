package com.library.management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="fines")
@Getter
@Setter
public class Fine extends BaseEntity{


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;



    @OneToOne
    @JoinColumn(name="borrow_detail_id")
    private BorrowDetail borrowDetail;



    private Double amount;



    private String reason;



    private Boolean paid;



    private LocalDateTime paidAt;


}
