package com.library.management.exception;

import lombok.Getter;

/**
 * Exception dùng khi gửi yêu cầu mượn sách (createBorrowRequest).
 * Mang theo bookId để handler redirect về đúng trang chi tiết sách.
 */
@Getter
public class BorrowSubmitException extends RuntimeException {

    private final Long bookId;

    public BorrowSubmitException(Long bookId, String message) {
        super(message);
        this.bookId = bookId;
    }
}
