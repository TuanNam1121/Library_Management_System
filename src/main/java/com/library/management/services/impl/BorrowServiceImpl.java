package com.library.management.services.impl;

import com.library.management.entities.*;
import com.library.management.enums.BookStatus;
import com.library.management.enums.BorrowItemStatus;
import com.library.management.enums.BorrowStatus;
import com.library.management.exception.BorrowOperationException;
import com.library.management.exception.BorrowSubmitException;
import com.library.management.repositories.BookRepository;
import com.library.management.repositories.BorrowDetailRepository;
import com.library.management.repositories.BorrowRequestRepository;
import com.library.management.repositories.FineRepository;
import com.library.management.repositories.UserRepository;
import com.library.management.services.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowDetailRepository borrowDetailRepository;
    private final FineRepository fineRepository;

    @Override
    public BorrowRequest createBorrowRequest(String username, Long bookId) {
        User reader = userRepository.findByUsername(username)
                .orElseThrow(() -> new BorrowSubmitException(bookId, "Không tìm thấy độc giả với username: " + username));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BorrowSubmitException(bookId, "Không tìm thấy sách với id: " + bookId));

        if (book.getIsDeleted() != null && book.getIsDeleted()) {
            throw new BorrowSubmitException(bookId, "Sách này đã bị xóa khỏi hệ thống");
        }

        if (book.getStatus() != BookStatus.APPROVED) {
            throw new BorrowSubmitException(bookId, "Sách này hiện chưa được phê duyệt để mượn");
        }

        if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
            throw new BorrowSubmitException(bookId, "Sách hiện tại không có sẵn bản nào để mượn");
        }

        List<BorrowStatus> activeStatuses = List.of(BorrowStatus.PENDING, BorrowStatus.RESERVED,
                BorrowStatus.BORROWING);
        if (borrowRequestRepository.existsActiveBorrowRequest(username, bookId, activeStatuses)) {
            throw new BorrowSubmitException(bookId, "Bạn đã gửi yêu cầu mượn hoặc đang giữ chỗ/mượn cuốn sách này rồi.");
        }

        BorrowRequest request = new BorrowRequest();
        request.setReader(reader);
        request.setStatus(BorrowStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        request.setIsDeleted(false);

        BorrowDetail detail = new BorrowDetail();
        detail.setBorrowRequest(request);
        detail.setBook(book);

        List<BorrowDetail> details = new ArrayList<>();
        details.add(detail);
        request.setDetails(details);

        return borrowRequestRepository.save(request);
    }

    @Override
    public List<BorrowRequest> getBorrowHistory(String username) {
        return borrowRequestRepository.findAllByReaderUsernameOrderByRequestDateDesc(username);
    }

    @Override
    public List<BorrowDetail> getOverdueBooks(String username) {
        return borrowDetailRepository
                .findByBorrowRequestReaderUsernameAndReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(
                        username, LocalDateTime.now());
    }

    @Override
    public List<BorrowRequest> getPendingRequests() {
        return borrowRequestRepository.findAllByStatusOrderByRequestDateDesc(BorrowStatus.PENDING);
    }

    @Override
    public List<BorrowRequest> getAllRequests() {
        return borrowRequestRepository.findAllByOrderByRequestDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRequest> searchRequests(String keyword, BorrowStatus status) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String numericKeyword = normalizedKeyword.startsWith("#")
                ? normalizedKeyword.substring(1).trim()
                : normalizedKeyword;

        Long requestId = -1L;
        try {
            requestId = Long.valueOf(numericKeyword);
        } catch (NumberFormatException ignored) {
        }

        return borrowRequestRepository.searchManagementRequests(
                normalizedKeyword,
                requestId,
                status
        );
    }

    @Override
    public BorrowRequest getRequestById(Long id) {
        return borrowRequestRepository.findById(id)
                .orElseThrow(() -> new BorrowOperationException(id, "Không tìm thấy yêu cầu mượn với id: " + id));
    }

    @Override
    public void approveRequest(Long requestId, String librarianUsername) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy yêu cầu mượn với id: " + requestId));

        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new BorrowOperationException(requestId, "Yêu cầu mượn này đã được xử lý từ trước");
        }

        User librarian = userRepository.findByUsername(librarianUsername)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy thủ thư với username: " + librarianUsername));

        // Process details
        if (request.getDetails() != null) {
            for (BorrowDetail detail : request.getDetails()) {
                Book book = detail.getBook();
                if (Boolean.TRUE.equals(book.getIsDeleted())) {
                    throw new RuntimeException("Sách '" + book.getTitle() + "' đã bị xóa khỏi hệ thống");
                }
                if (book.getAvailableQuantity() == null || book.getAvailableQuantity() <= 0) {
                    throw new BorrowOperationException(requestId, "Sách '" + book.getTitle() + "' đã hết, không thể phê duyệt");
                }
                book.setAvailableQuantity(book.getAvailableQuantity() - 1);
                bookRepository.save(book);

                detail.setReservedAt(LocalDateTime.now());
                detail.setStatus(null); // Reserved but not yet borrowing
            }
        }

        request.setStatus(BorrowStatus.RESERVED);
        request.setLibrarian(librarian);
        request.setApprovedAt(LocalDateTime.now());

        borrowRequestRepository.save(request);
    }

    @Override
    public void confirmPickup(Long requestId, String librarianUsername) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy yêu cầu mượn với id: " + requestId));

        if (request.getStatus() != BorrowStatus.RESERVED) {
            throw new BorrowOperationException(requestId, "Yêu cầu mượn này không ở trạng thái giữ chỗ để nhận sách");
        }

        User librarian = userRepository.findByUsername(librarianUsername)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy thủ thư với username: " + librarianUsername));

        if (request.getDetails() != null) {
            for (BorrowDetail detail : request.getDetails()) {
                detail.setBorrowDate(LocalDateTime.now());
                // detail.setDueDate(LocalDateTime.now().plusDays(5));
                detail.setDueDate(LocalDateTime.now().plusMinutes(5));
                detail.setStatus(BorrowItemStatus.BORROWING);
            }
        }

        request.setStatus(BorrowStatus.BORROWING);
        request.setLibrarian(librarian);

        borrowRequestRepository.save(request);
    }

    @Override
    public void cancelReservation(Long requestId, String librarianUsername) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy yêu cầu mượn với id: " + requestId));

        if (request.getStatus() != BorrowStatus.RESERVED) {
            throw new BorrowOperationException(requestId, "Yêu cầu mượn này không ở trạng thái giữ chỗ để hủy");
        }

        User librarian = userRepository.findByUsername(librarianUsername)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy thủ thư với username: " + librarianUsername));

        if (request.getDetails() != null) {
            for (BorrowDetail detail : request.getDetails()) {
                Book book = detail.getBook();
                book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                bookRepository.save(book);
            }
        }

        request.setStatus(BorrowStatus.CANCELLED);
        request.setLibrarian(librarian);

        borrowRequestRepository.save(request);
    }

    @Override
    public void rejectRequest(Long requestId, String librarianUsername) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy yêu cầu mượn với id: " + requestId));

        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new BorrowOperationException(requestId, "Yêu cầu mượn này đã được xử lý từ trước");
        }

        User librarian = userRepository.findByUsername(librarianUsername)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy thủ thư với username: " + librarianUsername));

        request.setStatus(BorrowStatus.REJECTED);
        request.setLibrarian(librarian);
        request.setApprovedAt(LocalDateTime.now());

        borrowRequestRepository.save(request);
    }

    @Override
    public boolean recordReturn(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy yêu cầu mượn với id: " + requestId));

        if (request.getStatus() != BorrowStatus.BORROWING) {
            throw new BorrowOperationException(requestId, "Không thể thực hiện trả cho yêu cầu mượn ở trạng thái này");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean hasFine = false;

        if (request.getDetails() != null) {
            for (BorrowDetail detail : request.getDetails()) {
                detail.setReturnDate(now);

                // Calculate fine
                long overdue = Duration.between(detail.getDueDate(), now).toMinutes();
                if (overdue > 0) {
                    double fineAmount = overdue * 1000.0;

                    Fine fine = new Fine();
                    fine.setBorrowDetail(detail);
                    fine.setAmount(fineAmount);
                    fine.setReason(
                            "Muon qua han " + overdue + "phut (Moi phut 10.000VND)");
                    fine.setPaid(false);
                    fine.setIsDeleted(false);

                    fineRepository.save(fine);
                    detail.setFine(fine);
                    detail.setStatus(BorrowItemStatus.OVERDUE);
                    hasFine = true;
                } else {
                    detail.setStatus(BorrowItemStatus.RETURNED);
                    Book book = detail.getBook();
                    book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                    bookRepository.save(book);
                }
            }
        }

        if (!hasFine) {
            request.setStatus(BorrowStatus.RETURNED);
        }

        borrowRequestRepository.save(request);
        return hasFine;
    }

    @Override
    public void payFine(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new BorrowOperationException(requestId, "Không tìm thấy yêu cầu mượn với id: " + requestId));

        if (request.getStatus() != BorrowStatus.BORROWING) {
            throw new BorrowOperationException(requestId, "Yêu cầu mượn này không ở trạng thái đang mượn để thanh toán phạt");
        }

        if (request.getDetails() != null) {
            for (BorrowDetail detail : request.getDetails()) {
                Fine fine = detail.getFine();
                if (fine != null && !fine.getPaid()) {
                    fine.setPaid(true);
                    fine.setPaidAt(LocalDateTime.now());
                    fineRepository.save(fine);

                    detail.setStatus(BorrowItemStatus.RETURNED);
                    Book book = detail.getBook();
                    book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                    bookRepository.save(book);
                }
            }
        }

        // Set request status to RETURNED
        request.setStatus(BorrowStatus.RETURNED);
        borrowRequestRepository.save(request);
    }

    @Override
    public List<BorrowDetail> findHistoryByReader(long id) {
        return borrowDetailRepository.findHistoryByReader(id);
    }
}
