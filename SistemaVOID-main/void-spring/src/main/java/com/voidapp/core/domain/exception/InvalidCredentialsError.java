package com.voidapp.core.domain.exception;

public class InvalidCredentialsError extends DomainException {
    public InvalidCredentialsError() {
        super("Credenciales inválidas");
    }
}
