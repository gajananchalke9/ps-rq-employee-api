package com.reliaquest.api.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(final String message) {
        super(message);
    }
}
