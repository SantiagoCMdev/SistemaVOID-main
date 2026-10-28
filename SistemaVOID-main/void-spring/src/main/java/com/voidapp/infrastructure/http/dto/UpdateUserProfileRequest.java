package com.voidapp.infrastructure.http.dto;

public record UpdateUserProfileRequest(
        String fullName,
        String email,
        String password
) {}
