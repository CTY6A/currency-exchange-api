package com.stubedavd.exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException() {
    }

    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
