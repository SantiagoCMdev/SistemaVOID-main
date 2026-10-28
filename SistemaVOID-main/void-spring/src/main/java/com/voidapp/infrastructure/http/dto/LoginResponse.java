package com.voidapp.infrastructure.http.dto;

import com.voidapp.domain.model.UserRole;
import java.util.UUID;

public record LoginResponse(String token, UserDto user) {
    public record UserDto(UUID id, String email, String fullName, UserRole role) {}
}
