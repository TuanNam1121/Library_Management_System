package com.library.management.repositories;

import com.library.management.entities.BorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowDetailRepository extends JpaRepository<BorrowDetail, Long> {
    Optional<BorrowDetail> findByIdAndBorrowRequestReaderUsername(Long id, String username);
}
