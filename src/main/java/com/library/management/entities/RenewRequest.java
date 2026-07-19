package com.library.management.entities;

import com.library.management.enums.RenewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RenewRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_detail_id", nullable = false)
    private BorrowDetail borrowDetail;

    @Column(name = "current_due_date", nullable = false)
    private LocalDateTime currentDueDate;

    @Column(name = "requested_due_date", nullable = false)
    private LocalDateTime requestedDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RenewStatus status;

    @Column(name = "reader_note", length = 500)
    private String readerNote;

    @Column(name = "librarian_note", length = 500)
    private String librarianNote;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;
}
