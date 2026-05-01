package com.warehouse.exception;

public class NoBulletsException extends RuntimeException {
    public NoBulletsException(String message) {
        super(message);
    }
}
