package com.voidapp.infrastructure.http.controller;

import com.voidapp.core.application.usecase.UpdateUserProfileUseCase;
import com.voidapp.core.application.usecase.UpdateUserSettingsUseCase;
import com.voidapp.domain.model.UserSettings;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.http.dto.UpdateUserProfileRequest;
import com.voidapp.infrastructure.http.dto.UpdateUserSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserRestController {

    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UpdateUserSettingsUseCase updateUserSettingsUseCase;

    @PutMapping("/profile")
    public ResponseEntity<LoginResponse.UserDto> updateProfile(
            @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {
        
        UUID userId = (UUID) authentication.getPrincipal();
        LoginResponse.UserDto response = updateUserProfileUseCase.execute(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/settings")
    public ResponseEntity<UserSettings> updateSettings(
            @RequestBody UpdateUserSettingsRequest request,
            Authentication authentication) {
        
        UUID userId = (UUID) authentication.getPrincipal();
        UserSettings response = updateUserSettingsUseCase.execute(userId, request);
        return ResponseEntity.ok(response);
    }
}
