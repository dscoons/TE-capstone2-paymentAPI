package com.techelevator.tenmo.exceptions;

public class TenmoAccountNotFoundException extends RuntimeException{
    public TenmoAccountNotFoundException() {
        super();
    }
    public TenmoAccountNotFoundException(String message) {
        super(message);
    }
    public TenmoAccountNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}

