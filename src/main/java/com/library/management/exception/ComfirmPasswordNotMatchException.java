package com.library.management.exception;

public class ComfirmPasswordNotMatchException extends RuntimeException {
    public ComfirmPasswordNotMatchException(String message) {
        super(message);
    }
}
