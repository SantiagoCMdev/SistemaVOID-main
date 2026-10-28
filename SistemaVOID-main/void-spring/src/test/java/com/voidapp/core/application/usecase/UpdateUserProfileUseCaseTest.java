package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.DuplicateEmailError;
import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserRole;
import com.voidapp.infrastructure.http.dto.LoginResponse;
import com.voidapp.infrastructure.http.dto.UpdateUserProfileRequest;
import com.voidapp.infrastructure.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @Test
    @DisplayName("Debe actualizar nombre, email y contraseña exitosamente")
    void shouldUpdateProfileSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("original@voidapp.com")
                .fullName("Nombre Original")
                .passwordHash("old_hash")
                .role(UserRole.USER)
                .build();

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nombre Actualizado",
                "nuevo@voidapp.com",
                "NewPassword456!"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("nuevo@voidapp.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("NewPassword456!")).thenReturn("new_hash_encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoginResponse.UserDto result = updateUserProfileUseCase.execute(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("nuevo@voidapp.com");
        assertThat(result.fullName()).isEqualTo("Nombre Actualizado");
        assertThat(existingUser.getPasswordHash()).isEqualTo("new_hash_encoded");

        verify(passwordEncoder).encode("NewPassword456!");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Debe actualizar solo el nombre cuando no se proveen email ni password")
    void shouldUpdateOnlyNameWhenEmailAndPasswordAreNotProvided() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("usuario@voidapp.com")
                .fullName("Nombre Anterior")
                .passwordHash("hash_existente")
                .role(UserRole.USER)
                .build();

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Solo Nombre Nuevo",
                null,
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoginResponse.UserDto result = updateUserProfileUseCase.execute(userId, request);

        // Assert
        assertThat(result.fullName()).isEqualTo("Solo Nombre Nuevo");
        assertThat(result.email()).isEqualTo("usuario@voidapp.com");

        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundError cuando el usuario a actualizar no existe")
    void shouldThrowUserNotFoundErrorWhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("Nombre", "email@void.com", "pass");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateUserProfileUseCase.execute(userId, request))
                .isInstanceOf(UserNotFoundError.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar DuplicateEmailError cuando el nuevo email ya está en uso por otro usuario")
    void shouldThrowDuplicateEmailErrorWhenEmailAlreadyInUse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .email("user1@voidapp.com")
                .fullName("User Uno")
                .build();

        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .email("duplicado@voidapp.com")
                .build();

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "User Modificado",
                "duplicado@voidapp.com",
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("duplicado@voidapp.com")).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThatThrownBy(() -> updateUserProfileUseCase.execute(userId, request))
                .isInstanceOf(DuplicateEmailError.class)
                .hasMessage("El correo electrónico ya está registrado");

        verify(userRepository, never()).save(any());
    }
}

