package com.library.management.repositories;

import com.library.management.entities.BorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowDetailRepository extends JpaRepository<BorrowDetail, Long> {
}
