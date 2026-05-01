package com.warehouse.exception;

public class BrokenItemException extends RuntimeException {
    public BrokenItemException(String message) {
        super(message);
    }
}
