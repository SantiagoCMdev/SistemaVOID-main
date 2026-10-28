package com.voidapp.core.domain.exception;

public class ActiveSubscriptionExistsError extends DomainException {
    public ActiveSubscriptionExistsError(String message) {
        super(message);
    }
}
