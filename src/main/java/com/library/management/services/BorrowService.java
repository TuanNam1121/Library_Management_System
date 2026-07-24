package com.library.management.services;

import com.library.management.entities.BorrowDetail;
import com.library.management.entities.BorrowRequest;
import com.library.management.enums.BorrowStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowService {
    BorrowRequest createBorrowRequest(String username, Long bookId);
    List<BorrowRequest> getBorrowHistory(String username);
    List<BorrowDetail> getOverdueBooks(String username);
    List<BorrowRequest> getPendingRequests();
    List<BorrowRequest> getAllRequests();
    Page<BorrowRequest> searchRequests(String keyword, BorrowStatus status, Pageable pageable);
    BorrowRequest getRequestById(Long id);
    void approveRequest(Long requestId, String librarianUsername);
    void rejectRequest(Long requestId, String librarianUsername);
    void confirmPickup(Long requestId, String librarianUsername);
    void cancelReservation(Long requestId, String librarianUsername);
    boolean recordReturn(Long requestId);
    void payFine(Long requestId);
    List<BorrowDetail> findHistoryByReader(long id);
}
