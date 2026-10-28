package com.voidapp.infrastructure.http.controller;

import com.voidapp.core.application.usecase.LoginUserUseCase;
import com.voidapp.infrastructure.http.dto.LoginRequest;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final com.voidapp.core.application.usecase.RegisterUserUseCase registerUserUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginUserUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse.UserDto> register(@RequestBody com.voidapp.infrastructure.http.dto.RegisterRequest request) {
        LoginResponse.UserDto response = registerUserUseCase.execute(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }
}
