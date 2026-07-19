package com.library.management.exception;

public class NotFoundBorrowDetailIdException extends RenewBusinessException {
    public NotFoundBorrowDetailIdException(Long borrowDetailId,String message) {
        super(borrowDetailId,message);
    }
}
