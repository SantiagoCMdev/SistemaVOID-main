package com.voidapp.core.domain.exception;

public class ConcurrentStreamLimitExceededError extends DomainException {
    public ConcurrentStreamLimitExceededError(String message) {
        super(message);
    }
}
