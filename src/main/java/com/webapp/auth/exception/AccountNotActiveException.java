package com.webapp.auth.exception;

public class AccountNotActiveException extends RuntimeException {
    
    public AccountNotActiveException(String message) {
        super(message);
    }
    
    public AccountNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}

