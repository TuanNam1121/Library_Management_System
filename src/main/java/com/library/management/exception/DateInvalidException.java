package com.library.management.exception;

public class DateInvalidException extends RenewBusinessException {
    public DateInvalidException(Long borrowDetailId,String message) {
        super(borrowDetailId,message);
    }
}
