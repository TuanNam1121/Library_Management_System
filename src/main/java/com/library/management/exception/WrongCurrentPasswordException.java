package com.library.management.exception;

public class WrongCurrentPasswordException extends RuntimeException {
    public WrongCurrentPasswordException(String message) {
        super(message);
    }
}
