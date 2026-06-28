package com.library.management.repositories;

import com.library.management.entities.BorrowRequest;
import com.library.management.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findAllByReaderUsernameOrderByRequestDateDesc(String username);
    List<BorrowRequest> findAllByStatusOrderByRequestDateDesc(BorrowStatus status);
    List<BorrowRequest> findAllByOrderByRequestDateDesc();
}
