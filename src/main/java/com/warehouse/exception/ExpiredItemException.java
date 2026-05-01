package com.warehouse.exception;

public class ExpiredItemException extends RuntimeException {
    public ExpiredItemException(String message) {
        super(message);
    }
}
