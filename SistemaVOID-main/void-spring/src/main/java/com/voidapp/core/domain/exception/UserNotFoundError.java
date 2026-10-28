package com.voidapp.core.domain.exception;

public class UserNotFoundError extends DomainException {
    public UserNotFoundError(String message) {
        super(message);
    }
}
