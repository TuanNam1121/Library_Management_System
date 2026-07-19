package com.library.management.exception;

public class ReturnDateInvalidException extends RenewBusinessException {
    public ReturnDateInvalidException(Long borrowDetailId ,String message) {
        super(borrowDetailId,message);
    }
}
