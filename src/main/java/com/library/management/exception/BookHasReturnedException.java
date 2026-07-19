package com.library.management.exception;

public class BookHasReturnedException extends RenewBusinessException {
    public BookHasReturnedException(Long borrowDetailId,String message) {
        super(borrowDetailId,message);
    }
}
