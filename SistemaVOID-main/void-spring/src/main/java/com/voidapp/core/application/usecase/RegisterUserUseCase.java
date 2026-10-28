package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.DuplicateEmailError;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserRole;
import com.voidapp.domain.model.UserSettings;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.http.dto.RegisterRequest;
import com.voidapp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse.UserDto execute(RegisterRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new DuplicateEmailError();
        }

        String passwordHash = passwordEncoder.encode(dto.password());

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordHash)
                .fullName(dto.fullName())
                .role(UserRole.USER)
                .build();

        UserSettings settings = UserSettings.builder()
                .user(user)
                .language("es")
                .theme("dark")
                .notifications(true)
                .autoplay(true)
                .build();

        user.setSettings(settings);
        user = userRepository.save(user);
        
        return new LoginResponse.UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
