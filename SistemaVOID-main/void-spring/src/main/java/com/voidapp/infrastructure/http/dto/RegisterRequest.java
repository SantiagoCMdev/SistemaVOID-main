package com.voidapp.infrastructure.http.dto;

public record RegisterRequest(String email, String password, String fullName) {
}
