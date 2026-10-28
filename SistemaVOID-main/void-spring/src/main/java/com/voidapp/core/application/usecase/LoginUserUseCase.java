package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.InvalidCredentialsError;
import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.User;
import com.voidapp.infrastructure.http.dto.LoginRequest;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.repository.UserRepository;
import com.voidapp.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse execute(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundError("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsError();
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());

        return new LoginResponse(
                token,
                new LoginResponse.UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole())
        );
    }
}
