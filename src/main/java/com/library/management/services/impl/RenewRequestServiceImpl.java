package com.library.management.services.impl;

import com.library.management.dto.RenewRequestDTO;
import com.library.management.entities.BorrowDetail;
import com.library.management.entities.RenewRequest;
import com.library.management.entities.User;
import com.library.management.enums.BorrowItemStatus;
import com.library.management.enums.RenewStatus;
import com.library.management.exception.*;
import com.library.management.repositories.BorrowDetailRepository;
import com.library.management.repositories.RenewRequestRepository;
import com.library.management.repositories.UserRepository;
import com.library.management.services.RenewRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RenewRequestServiceImpl implements RenewRequestService {
    private final RenewRequestRepository renewRequestRepository;
    private final BorrowDetailRepository borrowDetailRepository;
    private final UserRepository userRepository;
    @Override
    @Transactional
    public void createRequest(Long borrowDetailId, String username, RenewRequestDTO renewRequestDTO) {
        BorrowDetail borrowDetail = borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(
                        borrowDetailId,
                        username
                )
                .orElseThrow(() ->
                        new NotFoundBorrowDetailIdException(borrowDetailId, "Không tìm thấy phiếu mượn của người dùng"
                        )
                );

        if (borrowDetail.getStatus() != BorrowItemStatus.BORROWING) {
            throw new BookMustBeBorrowedException(borrowDetailId, "Chỉ sách đang mượn mới được gia hạn");
        }

        if (borrowDetail.getReturnDate() != null) {
            throw new BookHasReturnedException(borrowDetailId, "Sách này đã được trả");
        }

        if (renewRequestDTO.getRequestDueDate() == null) {
            throw new DateInvalidException(borrowDetailId,"Vui lòng chọn ngày trả mới");
        }

        if (!renewRequestDTO.getRequestDueDate()
                .isAfter(borrowDetail.getDueDate())) {
            throw new ReturnDateInvalidException(borrowDetailId,"Ngày trả mới phải sau ngày trả hiện tại");
        }

        if (renewRequestDTO.getRequestDueDate().isAfter(borrowDetail.getDueDate().plusDays(14))) {
            throw new TimeBorrowBookInvalidException(borrowDetailId,"Chỉ được gia hạn tối đa 14 ngày");
        }

        boolean pendingExists =
                renewRequestRepository
                        .existsByBorrowDetailIdAndStatus(
                                borrowDetailId,
                                RenewStatus.PENDING
                        );

        if (pendingExists) {
            throw new RenewRequestInvalidException(borrowDetailId,"Bạn đã có một yêu cầu gia hạn đang chờ duyệt");
        }

        RenewRequest renewRequest = new RenewRequest();

        renewRequest.setBorrowDetail(borrowDetail);
        renewRequest.setCurrentDueDate(borrowDetail.getDueDate());
        renewRequest.setRequestedDueDate(renewRequestDTO.getRequestDueDate());
        renewRequest.setReaderNote(renewRequestDTO.getReaderNote());
        renewRequest.setStatus(RenewStatus.PENDING);
        renewRequest.setRequestedAt(LocalDateTime.now());

        renewRequestRepository.save(renewRequest);
    }

    @Override
    @Transactional
    public void approve(Long renewRequestId, String librarianUsername, String librarianNote) {
        RenewRequest renewRequest = renewRequestRepository.findByIdAndStatus(renewRequestId,RenewStatus.PENDING).orElseThrow(()->new InvalidRenewRequestException("Invalid renew request"));
        User libUser = userRepository.findByUsername(librarianUsername).orElseThrow(()-> new InvalidLibrarianException("Not found Librarian"));
        if(libUser.getRole() == null || !"LIBRARIAN".equals(libUser.getRole().getName())) {
            throw new InvalidLibrarianException("Librarian is not a LIBRARIAN");
        }
        BorrowDetail borrowDetail = renewRequest.getBorrowDetail();
        if(borrowDetail.getStatus() != BorrowItemStatus.BORROWING) {
            throw new BookHasReturnedException(borrowDetail.getId(),"Invalid book status");
        }
        if(borrowDetail.getReturnDate() != null) {
            throw new BookHasReturnedException(borrowDetail.getId(), "Book has been returned");
        }
        borrowDetail.setDueDate(renewRequest.getRequestedDueDate());
        renewRequest.setStatus(RenewStatus.APPROVED);
        renewRequest.setLibrarianNote(librarianNote);
        renewRequest.setProcessedAt(LocalDateTime.now());
        renewRequest.setProcessedBy(libUser);
        borrowDetailRepository.save(borrowDetail);
        renewRequestRepository.save(renewRequest);
    }

    @Override
    @Transactional
    public void reject(Long renewRequestId, String librarianUsername, String librarianNote) {
        RenewRequest renewRequest = renewRequestRepository.findByIdAndStatus(renewRequestId, RenewStatus.PENDING).orElseThrow(() -> new InvalidRenewRequestException("Yêu cầu gia hạn không tồn tại hoặc đã được xử lý"));
        User librarian = userRepository.findByUsername(librarianUsername).orElseThrow(() -> new InvalidLibrarianException("Không tìm thấy librarian"));

        if (librarian.getRole() == null || !"LIBRARIAN".equals(librarian.getRole().getName())){
            throw new InvalidLibrarianException("Bạn không có quyền từ chối yêu cầu");
        }

        renewRequest.setStatus(RenewStatus.REJECTED);

        renewRequest.setLibrarianNote(librarianNote);

        renewRequest.setProcessedAt(LocalDateTime.now());

        renewRequest.setProcessedBy(librarian);

        renewRequestRepository.save(renewRequest);
    }
    }




