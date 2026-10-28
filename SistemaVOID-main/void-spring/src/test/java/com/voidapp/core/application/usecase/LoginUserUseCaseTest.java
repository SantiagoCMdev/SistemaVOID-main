package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.InvalidCredentialsError;
import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserRole;
import com.voidapp.infrastructure.http.dto.LoginRequest;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.repository.UserRepository;
import com.voidapp.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private LoginUserUseCase loginUserUseCase;

    @Test
    @DisplayName("Debe autenticar exitosamente y retornar token JWT y datos de usuario")
    void shouldLoginSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LoginRequest request = new LoginRequest("admin@voidapp.com", "CorrectPassword123");
        User user = User.builder()
                .id(userId)
                .email("admin@voidapp.com")
                .passwordHash("$2a$10$hashedPassword")
                .fullName("Admin Void")
                .role(UserRole.ADMIN)
                .build();

        String expectedToken = "jwt.header.payload.signature";

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(userId, UserRole.ADMIN)).thenReturn(expectedToken);

        // Act
        LoginResponse response = loginUserUseCase.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(expectedToken);
        assertThat(response.user()).isNotNull();
        assertThat(response.user().id()).isEqualTo(userId);
        assertThat(response.user().email()).isEqualTo("admin@voidapp.com");
        assertThat(response.user().fullName()).isEqualTo("Admin Void");
        assertThat(response.user().role()).isEqualTo(UserRole.ADMIN);

        verify(jwtTokenProvider).generateToken(userId, UserRole.ADMIN);
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundError cuando el email no corresponde a ningún usuario")
    void shouldThrowUserNotFoundErrorWhenUserDoesNotExist() {
        // Arrange
        LoginRequest request = new LoginRequest("inexistente@voidapp.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(request))
                .isInstanceOf(UserNotFoundError.class)
                .hasMessage("Credenciales inválidas");

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("Debe lanzar InvalidCredentialsError cuando la contraseña no coincide")
    void shouldThrowInvalidCredentialsErrorWhenPasswordIsIncorrect() {
        // Arrange
        LoginRequest request = new LoginRequest("user@voidapp.com", "WrongPassword");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@voidapp.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(UserRole.USER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(request))
                .isInstanceOf(InvalidCredentialsError.class)
                .hasMessage("Credenciales inválidas");

        verifyNoInteractions(jwtTokenProvider);
    }
}

