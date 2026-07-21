package com.library.management.exception;

import lombok.Getter;

/**
 * Exception dùng cho các thao tác trên BorrowRequest đã tồn tại
 * (approve, reject, confirm-pickup, cancel, return, pay-fine).
 * Mang theo requestId để handler redirect về đúng trang chi tiết.
 */
@Getter
public class BorrowOperationException extends RuntimeException {

    private final Long requestId;

    public BorrowOperationException(Long requestId, String message) {
        super(message);
        this.requestId = requestId;
    }
}
