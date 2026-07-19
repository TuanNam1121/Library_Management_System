package com.library.management.exception;

public class GmailAlreadyExistException extends RuntimeException {
    public GmailAlreadyExistException(String message) {
        super(message);
    }
}
