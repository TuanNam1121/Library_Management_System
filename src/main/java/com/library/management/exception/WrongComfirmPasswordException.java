package com.library.management.exception;

public class WrongComfirmPasswordException extends RuntimeException {
    public WrongComfirmPasswordException(String message) {
        super(message);
    }
}
