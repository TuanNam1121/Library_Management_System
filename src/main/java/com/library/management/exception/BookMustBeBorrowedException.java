package com.library.management.exception;

public class BookMustBeBorrowedException extends RenewBusinessException {
    public BookMustBeBorrowedException(Long borrowDetailId,String message) {
        super(borrowDetailId,message);
    }
}
