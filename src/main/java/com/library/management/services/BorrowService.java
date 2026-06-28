package com.library.management.services;

import com.library.management.entities.BorrowRequest;

import java.util.List;

public interface BorrowService {
    BorrowRequest createBorrowRequest(String username, Long bookId);
    List<BorrowRequest> getBorrowHistory(String username);
    List<BorrowRequest> getPendingRequests();
    List<BorrowRequest> getAllRequests();
    BorrowRequest getRequestById(Long id);
    void approveRequest(Long requestId, String librarianUsername);
    void rejectRequest(Long requestId, String librarianUsername);
    void recordReturn(Long requestId);
    void payFine(Long requestId);
}
