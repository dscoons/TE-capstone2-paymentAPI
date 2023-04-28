package com.techelevator.tenmo.exceptions;

public class TransferIdNotFoundException extends RuntimeException {
    public TransferIdNotFoundException() {
        super();
    }
    public TransferIdNotFoundException(String message) {
        super(message);
    }
    public TransferIdNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
