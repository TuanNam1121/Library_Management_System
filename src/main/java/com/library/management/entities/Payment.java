package com.library.management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FineId", nullable = false)
    private Fine fine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "Amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "PaymentMethod", nullable = false)
    private Integer paymentMethod;

    @ColumnDefault("1")
    @Column(name = "Status", nullable = false)
    private Integer status;

    @Nationalized
    @Column(name = "TransactionCode", length = 100)
    private String transactionCode;

    @Nationalized
    @Column(name = "Note")
    private String note;

    @ColumnDefault("getdate()")
    @Column(name = "PaymentDate")
    private Instant paymentDate;

}