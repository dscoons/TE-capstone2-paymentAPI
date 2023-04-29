package com.techelevator.tenmo.exceptions;

public class SelfTransferException extends RuntimeException {   
    public SelfTransferException() {
    super();
}
    public SelfTransferException(String message) {
        super(message);
    }
    public SelfTransferException(String message, Exception cause) {
        super(message, cause);
    }
}
