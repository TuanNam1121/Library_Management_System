package com.library.management.exception;

public class UsernameNotExistException extends RuntimeException {
    public UsernameNotExistException(String message) {
        super(message);
    }
}
