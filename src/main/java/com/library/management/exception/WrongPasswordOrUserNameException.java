package com.library.management.exception;

public class WrongPasswordOrUserNameException extends RuntimeException {
    public WrongPasswordOrUserNameException(String message) {
        super(message);
    }
}
