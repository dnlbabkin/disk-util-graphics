package com.reliab.disktransfer.exception;

public class TokenProcessingException extends RuntimeException {

    public TokenProcessingException() {
        super();
    }

    public TokenProcessingException(String message) {
        super(message);
    }

    public TokenProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenProcessingException(Throwable cause) {
        super(cause);
    }

    protected TokenProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
