package com.library.management.services;

import com.library.management.dto.RenewRequestDTO;
import com.library.management.entities.BorrowDetail;
import com.library.management.entities.RenewRequest;
import com.library.management.entities.Role;
import com.library.management.entities.User;
import com.library.management.enums.BorrowItemStatus;
import com.library.management.enums.RenewStatus;
import com.library.management.exception.*;
import com.library.management.repositories.BorrowDetailRepository;
import com.library.management.repositories.RenewRequestRepository;
import com.library.management.repositories.UserRepository;
import com.library.management.services.impl.RenewRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RenewRequestServiceImpl")
class RenewRequestServiceImplTest {

    @Mock
    private RenewRequestRepository renewRequestRepository;

    @Mock
    private BorrowDetailRepository borrowDetailRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RenewRequestServiceImpl service;

    private static final Long BORROW_DETAIL_ID = 100L;
    private static final Long RENEW_REQUEST_ID = 5L;
    private static final String READER = "reader1";
    private static final String LIBRARIAN = "lib1";

    private LocalDateTime currentDueDate;

    @BeforeEach
    void setUp() {
        currentDueDate = LocalDateTime.now().plusDays(3);
    }

    // ---------- helpers ----------

    private BorrowDetail borrowingDetail() {
        BorrowDetail bd = new BorrowDetail();
        bd.setId(BORROW_DETAIL_ID);
        bd.setStatus(BorrowItemStatus.BORROWING);
        bd.setDueDate(currentDueDate);
        bd.setReturnDate(null);
        return bd;
    }

    private RenewRequestDTO dtoWithDueDate(LocalDateTime requestDueDate) {
        RenewRequestDTO dto = new RenewRequestDTO();
        dto.setRequestDueDate(requestDueDate);
        dto.setReaderNote("Cần thêm thời gian đọc");
        return dto;
    }

    private User librarianUser() {
        Role role = new Role();
        role.setName("LIBRARIAN");
        User u = new User();
        u.setUsername(LIBRARIAN);
        u.setRole(role);
        return u;
    }

    private RenewRequest pendingRequest(BorrowDetail bd) {
        RenewRequest rr = new RenewRequest();
        rr.setId(RENEW_REQUEST_ID);
        rr.setBorrowDetail(bd);
        rr.setCurrentDueDate(bd.getDueDate());
        rr.setRequestedDueDate(bd.getDueDate().plusDays(7));
        rr.setStatus(RenewStatus.PENDING);
        rr.setRequestedAt(LocalDateTime.now());
        return rr;
    }

    // ============================================================
    // createRequest
    // ============================================================

    @Nested
    @DisplayName("createRequest")
    class CreateRequest {

        @Test
        @DisplayName("tạo yêu cầu gia hạn thành công với dữ liệu hợp lệ")
        void createRequest_success() {
            BorrowDetail bd = borrowingDetail();
            LocalDateTime requestDueDate = currentDueDate.plusDays(7);
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));
            when(renewRequestRepository.existsByBorrowDetailIdAndStatus(BORROW_DETAIL_ID, RenewStatus.PENDING))
                    .thenReturn(false);

            service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(requestDueDate));

            ArgumentCaptor<RenewRequest> captor = ArgumentCaptor.forClass(RenewRequest.class);
            verify(renewRequestRepository).save(captor.capture());
            RenewRequest saved = captor.getValue();

            assertThat(saved.getBorrowDetail()).isSameAs(bd);
            assertThat(saved.getCurrentDueDate()).isEqualTo(currentDueDate);
            assertThat(saved.getRequestedDueDate()).isEqualTo(requestDueDate);
            assertThat(saved.getReaderNote()).isEqualTo("Cần thêm thời gian đọc");
            assertThat(saved.getStatus()).isEqualTo(RenewStatus.PENDING);
            assertThat(saved.getRequestedAt()).isNotNull();
        }

        @Test
        @DisplayName("ném NotFoundBorrowDetailIdException khi không tìm thấy phiếu mượn")
        void createRequest_notFound() {
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate.plusDays(7))))
                    .isInstanceOf(NotFoundBorrowDetailIdException.class);

            verify(renewRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném BookMustBeBorrowedException khi sách không ở trạng thái BORROWING")
        void createRequest_notBorrowing() {
            BorrowDetail bd = borrowingDetail();
            bd.setStatus(BorrowItemStatus.RETURNED);
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));

            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate.plusDays(7))))
                    .isInstanceOf(BookMustBeBorrowedException.class);

            verify(renewRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném BookHasReturnedException khi sách đã có ngày trả")
        void createRequest_alreadyReturned() {
            BorrowDetail bd = borrowingDetail();
            bd.setReturnDate(LocalDateTime.now());
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));

            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate.plusDays(7))))
                    .isInstanceOf(BookHasReturnedException.class);
        }

        @Test
        @DisplayName("ném DateInvalidException khi ngày trả mới null")
        void createRequest_nullDueDate() {
            BorrowDetail bd = borrowingDetail();
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));

            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(null)))
                    .isInstanceOf(DateInvalidException.class);
        }

        @Test
        @DisplayName("ném ReturnDateInvalidException khi ngày trả mới không sau ngày trả hiện tại")
        void createRequest_dueDateNotAfterCurrent() {
            BorrowDetail bd = borrowingDetail();
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));

            // bằng đúng ngày hiện tại -> không "isAfter"
            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate)))
                    .isInstanceOf(ReturnDateInvalidException.class);
        }

        @Test
        @DisplayName("ném TimeBorrowBookInvalidException khi gia hạn quá 14 ngày")
        void createRequest_exceedsMaxRenewDays() {
            BorrowDetail bd = borrowingDetail();
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));

            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate.plusDays(15))))
                    .isInstanceOf(TimeBorrowBookInvalidException.class);
        }

        @Test
        @DisplayName("cho phép gia hạn đúng biên 14 ngày")
        void createRequest_exactly14Days() {
            BorrowDetail bd = borrowingDetail();
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));
            when(renewRequestRepository.existsByBorrowDetailIdAndStatus(BORROW_DETAIL_ID, RenewStatus.PENDING))
                    .thenReturn(false);

            service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate.plusDays(14)));

            verify(renewRequestRepository).save(any(RenewRequest.class));
        }

        @Test
        @DisplayName("ném RenewRequestInvalidException khi đã có yêu cầu PENDING")
        void createRequest_pendingAlreadyExists() {
            BorrowDetail bd = borrowingDetail();
            when(borrowDetailRepository.findByIdAndBorrowRequestReaderUsername(BORROW_DETAIL_ID, READER))
                    .thenReturn(Optional.of(bd));
            when(renewRequestRepository.existsByBorrowDetailIdAndStatus(BORROW_DETAIL_ID, RenewStatus.PENDING))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    service.createRequest(BORROW_DETAIL_ID, READER, dtoWithDueDate(currentDueDate.plusDays(7))))
                    .isInstanceOf(RenewRequestInvalidException.class);

            verify(renewRequestRepository, never()).save(any());
        }
    }

    // ============================================================
    // approve
    // ============================================================

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("duyệt thành công: cập nhật dueDate và trạng thái APPROVED")
        void approve_success() {
            BorrowDetail bd = borrowingDetail();
            RenewRequest rr = pendingRequest(bd);
            LocalDateTime requested = rr.getRequestedDueDate();
            User lib = librarianUser();

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(lib));

            service.approve(RENEW_REQUEST_ID, LIBRARIAN, "OK duyệt");

            assertThat(bd.getDueDate()).isEqualTo(requested);
            assertThat(rr.getStatus()).isEqualTo(RenewStatus.APPROVED);
            assertThat(rr.getLibrarianNote()).isEqualTo("OK duyệt");
            assertThat(rr.getProcessedAt()).isNotNull();
            assertThat(rr.getProcessedBy()).isSameAs(lib);
            verify(borrowDetailRepository).save(bd);
            verify(renewRequestRepository).save(rr);
        }

        @Test
        @DisplayName("ném InvalidRenewRequestException khi không có yêu cầu PENDING")
        void approve_requestNotFound() {
            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approve(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidRenewRequestException.class);

            verify(borrowDetailRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném InvalidLibrarianException khi không tìm thấy librarian")
        void approve_librarianNotFound() {
            BorrowDetail bd = borrowingDetail();
            RenewRequest rr = pendingRequest(bd);
            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approve(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidLibrarianException.class);
        }

        @Test
        @DisplayName("ném InvalidLibrarianException khi user không có role LIBRARIAN")
        void approve_notLibrarianRole() {
            BorrowDetail bd = borrowingDetail();
            RenewRequest rr = pendingRequest(bd);
            User notLib = librarianUser();
            notLib.getRole().setName("READER");

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(notLib));

            assertThatThrownBy(() -> service.approve(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidLibrarianException.class);

            verify(borrowDetailRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném InvalidLibrarianException khi user không có role (null)")
        void approve_nullRole() {
            BorrowDetail bd = borrowingDetail();
            RenewRequest rr = pendingRequest(bd);
            User noRole = new User();
            noRole.setUsername(LIBRARIAN);
            noRole.setRole(null);

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(noRole));

            assertThatThrownBy(() -> service.approve(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidLibrarianException.class);
        }

        @Test
        @DisplayName("ném BookHasReturnedException khi sách không còn BORROWING")
        void approve_bookNotBorrowing() {
            BorrowDetail bd = borrowingDetail();
            bd.setStatus(BorrowItemStatus.RETURNED);
            RenewRequest rr = pendingRequest(bd);
            User lib = librarianUser();

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(lib));

            assertThatThrownBy(() -> service.approve(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(BookHasReturnedException.class);

            verify(borrowDetailRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném BookHasReturnedException khi sách đã có returnDate")
        void approve_bookAlreadyReturned() {
            BorrowDetail bd = borrowingDetail();
            bd.setReturnDate(LocalDateTime.now());
            RenewRequest rr = pendingRequest(bd);
            User lib = librarianUser();

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(lib));

            assertThatThrownBy(() -> service.approve(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(BookHasReturnedException.class);
        }
    }

    // ============================================================
    // reject
    // ============================================================

    @Nested
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("từ chối thành công: trạng thái REJECTED, không đổi dueDate")
        void reject_success() {
            BorrowDetail bd = borrowingDetail();
            LocalDateTime originalDueDate = bd.getDueDate();
            RenewRequest rr = pendingRequest(bd);
            User lib = librarianUser();

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(lib));

            service.reject(RENEW_REQUEST_ID, LIBRARIAN, "Không đủ điều kiện");

            assertThat(rr.getStatus()).isEqualTo(RenewStatus.REJECTED);
            assertThat(rr.getLibrarianNote()).isEqualTo("Không đủ điều kiện");
            assertThat(rr.getProcessedAt()).isNotNull();
            assertThat(rr.getProcessedBy()).isSameAs(lib);
            assertThat(bd.getDueDate()).isEqualTo(originalDueDate);
            verify(renewRequestRepository).save(rr);
            verify(borrowDetailRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném InvalidRenewRequestException khi không có yêu cầu PENDING")
        void reject_requestNotFound() {
            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.reject(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidRenewRequestException.class);

            verify(renewRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném InvalidLibrarianException khi không tìm thấy librarian")
        void reject_librarianNotFound() {
            RenewRequest rr = pendingRequest(borrowingDetail());
            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.reject(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidLibrarianException.class);

            verify(renewRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("ném InvalidLibrarianException khi user không phải LIBRARIAN")
        void reject_notLibrarianRole() {
            RenewRequest rr = pendingRequest(borrowingDetail());
            User notLib = librarianUser();
            notLib.getRole().setName("READER");

            when(renewRequestRepository.findByIdAndStatus(RENEW_REQUEST_ID, RenewStatus.PENDING))
                    .thenReturn(Optional.of(rr));
            when(userRepository.findByUsername(LIBRARIAN)).thenReturn(Optional.of(notLib));

            assertThatThrownBy(() -> service.reject(RENEW_REQUEST_ID, LIBRARIAN, "note"))
                    .isInstanceOf(InvalidLibrarianException.class);

            verify(renewRequestRepository, never()).save(any());
        }
    }
}
