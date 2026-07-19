package com.library.management.repositories;

import com.library.management.entities.RenewRequest;
import com.library.management.enums.RenewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RenewRequestRepository
        extends JpaRepository<RenewRequest, Long> {

    boolean existsByBorrowDetailIdAndStatus(
            Long borrowDetailId,
            RenewStatus status
    );

    List<RenewRequest> findByStatusOrderByRequestedAtAsc(
            RenewStatus status
    );

    Optional<RenewRequest> findByIdAndStatus(
            Long id,
            RenewStatus status
    );
}