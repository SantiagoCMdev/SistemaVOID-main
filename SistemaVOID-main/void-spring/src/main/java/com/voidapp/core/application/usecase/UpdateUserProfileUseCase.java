package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.DuplicateEmailError;
import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.User;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.http.dto.UpdateUserProfileRequest;
import com.voidapp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @SuppressWarnings("null")
    public LoginResponse.UserDto execute(UUID userId, UpdateUserProfileRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundError("Usuario no encontrado"));

        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(dto.email());
            if (existingUser.isPresent()) {
                throw new DuplicateEmailError();
            }
            user.setEmail(dto.email());
        }

        if (dto.fullName() != null && !dto.fullName().isEmpty()) {
            user.setFullName(dto.fullName());
        }

        if (dto.password() != null && !dto.password().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        user = userRepository.save(user);

        return new LoginResponse.UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
