package com.library.management.repositories;

import com.library.management.entities.BorrowDetail;
import com.library.management.enums.BorrowItemStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowDetailRepository extends JpaRepository<BorrowDetail, Long> {

    boolean existsByBookCategoryIdAndStatusIn(Long categoryId, List<BorrowItemStatus> statuses);

    @Query("""
                FROM BorrowDetail bd
                WHERE bd.borrowRequest.reader.id = :readerId
                ORDER BY bd.borrowDate DESC
            """)
    List<BorrowDetail> findHistoryByReader(@Param("readerId") Long readerId);

    Optional<BorrowDetail> findByIdAndBorrowRequestReaderUsername(Long id, String username);

    // Sách quá hạn của độc giả: chưa trả (returnDate null) và đã qua hạn trả
    // (dueDate < now)
    List<BorrowDetail> findByBorrowRequestReaderUsernameAndReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(
            String username, LocalDateTime dateTime);

    long countByStatus(BorrowItemStatus status);

    long countByStatusAndDueDateBefore(BorrowItemStatus status, LocalDateTime dateTime);

    @Query("SELECT bd.book.id, COUNT(bd) FROM BorrowDetail bd GROUP BY bd.book.id ORDER BY COUNT(bd) DESC")
    List<Object[]> findTopBookIds(Pageable pageable);
}
