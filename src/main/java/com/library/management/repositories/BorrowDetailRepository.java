package com.library.management.repositories;

import com.library.management.entities.BorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowDetailRepository extends JpaRepository<BorrowDetail, Long> {

    @Query("""
        FROM BorrowDetail bd
        WHERE bd.borrowRequest.reader.id = :readerId
        ORDER BY bd.borrowDate DESC
    """)
    List<BorrowDetail> findHistoryByReader(@Param("readerId") Long readerId);
}
