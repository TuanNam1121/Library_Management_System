package com.library.management.exception;

import lombok.Getter;

@Getter
public class RenewBusinessException extends RuntimeException {

    private final Long borrowDetailId;

    public RenewBusinessException(
            Long borrowDetailId,
            String message
    ) {
        super(message);
        this.borrowDetailId = borrowDetailId;
    }
}