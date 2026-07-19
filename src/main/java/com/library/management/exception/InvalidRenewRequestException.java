package com.library.management.exception;

public class InvalidRenewRequestException extends RuntimeException {
    public InvalidRenewRequestException(String message) {
        super(message);
    }
}
