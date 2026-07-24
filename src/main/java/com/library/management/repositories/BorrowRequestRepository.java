package com.library.management.repositories;

import com.library.management.entities.BorrowRequest;
import com.library.management.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findAllByReaderUsernameOrderByRequestDateDesc(String username);
    List<BorrowRequest> findAllByStatusOrderByRequestDateDesc(BorrowStatus status);
    List<BorrowRequest> findAllByOrderByRequestDateDesc();

    @Query("""
        SELECT DISTINCT br
        FROM BorrowRequest br
        JOIN br.details bd
        LEFT JOIN bd.book b
        WHERE (:status IS NULL OR br.status = :status)
          AND (
              :keyword = ''
              OR br.id = :requestId
              OR LOWER(br.reader.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(br.reader.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY br.requestDate DESC
    """)
    Page<BorrowRequest> searchManagementRequests(@Param("keyword") String keyword, @Param("requestId") Long requestId,
                                                 @Param("status") BorrowStatus status, Pageable pageable);

    @Query("""
        SELECT COUNT(br) > 0 
        FROM BorrowRequest br JOIN br.details bd 
        WHERE br.reader.username = :username 
          AND bd.book.id = :bookId 
          AND br.status IN :statuses
    """)
    boolean existsActiveBorrowRequest(@Param("username") String username, @Param("bookId") Long bookId,
                                      @Param("statuses") List<BorrowStatus> statuses);
}
