package com.techelevator.tenmo.exceptions;

public class LowAccountBalanceException extends RuntimeException {
    public LowAccountBalanceException() {
        super();
    }
    public LowAccountBalanceException(String message) {
        super(message);
    }
    public LowAccountBalanceException(String message, Exception cause) {
        super(message, cause);
    }
}

