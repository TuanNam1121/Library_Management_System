package com.library.management.exception;

public class RenewRequestInvalidException extends RenewBusinessException {
    public RenewRequestInvalidException(Long borrowDetailId,String message) {
        super(borrowDetailId,message);
    }
}
