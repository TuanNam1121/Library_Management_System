package com.library.management;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "BorrowRecordDetails")
public class BorrowRecordDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DetailId", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RecordId", nullable = false)
    private BorrowRecord record;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BookId", nullable = false)
    private Book book;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @ColumnDefault("0")
    @Column(name = "ReturnedQuantity")
    private Integer returnedQuantity;

    @Column(name = "ReturnDate")
    private LocalDate returnDate;

    @ColumnDefault("1")
    @Column(name = "Status", nullable = false)
    private Integer status;

}