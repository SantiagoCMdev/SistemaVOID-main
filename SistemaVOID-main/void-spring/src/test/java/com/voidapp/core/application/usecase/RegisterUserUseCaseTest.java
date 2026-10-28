package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.DuplicateEmailError;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserRole;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.http.dto.RegisterRequest;
import com.voidapp.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    @Test
    @DisplayName("Debe registrar un nuevo usuario exitosamente con configuración por defecto")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        RegisterRequest request = new RegisterRequest("usuario@ejemplo.com", "Password123!", "Juan Perez");
        UUID expectedId = UUID.randomUUID();
        String hashedPassword = "encoded_hash_12345";

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(expectedId);
            return toSave;
        });

        // Act
        LoginResponse.UserDto result = registerUserUseCase.execute(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(expectedId);
        assertThat(result.email()).isEqualTo("usuario@ejemplo.com");
        assertThat(result.fullName()).isEqualTo("Juan Perez");
        assertThat(result.role()).isEqualTo(UserRole.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getPasswordHash()).isEqualTo(hashedPassword);
        assertThat(savedUser.getSettings()).isNotNull();
        assertThat(savedUser.getSettings().getLanguage()).isEqualTo("es");
        assertThat(savedUser.getSettings().getTheme()).isEqualTo("dark");
        assertThat(savedUser.getSettings().getNotifications()).isTrue();
        assertThat(savedUser.getSettings().getAutoplay()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar DuplicateEmailError cuando el email ya está registrado")
    void shouldThrowDuplicateEmailErrorWhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existente@ejemplo.com", "Password123!", "Usuario Existente");
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("existente@ejemplo.com")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(request))
                .isInstanceOf(DuplicateEmailError.class)
                .hasMessage("El correo electrónico ya está registrado");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}

