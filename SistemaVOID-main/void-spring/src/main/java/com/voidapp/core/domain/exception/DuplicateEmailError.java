package com.voidapp.core.domain.exception;

public class DuplicateEmailError extends DomainException {
    public DuplicateEmailError() {
        super("El correo electrónico ya está registrado");
    }
}
