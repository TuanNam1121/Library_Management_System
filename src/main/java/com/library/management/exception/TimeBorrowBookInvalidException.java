package com.library.management.exception;

public class TimeBorrowBookInvalidException extends RenewBusinessException {
    public TimeBorrowBookInvalidException(Long borrowDetailId,String message) {
        super(borrowDetailId,message);
    }
}
