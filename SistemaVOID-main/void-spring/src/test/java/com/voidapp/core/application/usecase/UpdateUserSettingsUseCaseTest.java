package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserSettings;
import com.voidapp.infrastructure.http.dto.UpdateUserSettingsRequest;
import com.voidapp.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserSettingsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserSettingsUseCase updateUserSettingsUseCase;

    @Test
    @DisplayName("Debe actualizar ajustes existentes exitosamente")
    void shouldUpdateExistingSettingsSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("user@voidapp.com").build();

        UserSettings existingSettings = UserSettings.builder()
                .user(user)
                .language("es")
                .theme("dark")
                .notifications(true)
                .autoplay(true)
                .build();
        user.setSettings(existingSettings);

        UpdateUserSettingsRequest request = new UpdateUserSettingsRequest("en", "light", false, false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserSettings result = updateUserSettingsUseCase.execute(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLanguage()).isEqualTo("en");
        assertThat(result.getTheme()).isEqualTo("light");
        assertThat(result.getNotifications()).isFalse();
        assertThat(result.getAutoplay()).isFalse();

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Debe inicializar ajustes si el usuario no tenía ajustes previos")
    void shouldInitializeSettingsIfUserHadNone() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).settings(null).build();

        UpdateUserSettingsRequest request = new UpdateUserSettingsRequest("fr", "dark", true, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserSettings result = updateUserSettingsUseCase.execute(userId, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getLanguage()).isEqualTo("fr");
        assertThat(result.getTheme()).isEqualTo("dark");

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Debe mantener valores previos cuando los campos del request son nulos")
    void shouldKeepPreviousValuesWhenFieldsAreNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        UserSettings existingSettings = UserSettings.builder()
                .user(user)
                .language("es")
                .theme("dark")
                .notifications(true)
                .autoplay(true)
                .build();
        user.setSettings(existingSettings);

        // Solo se actualiza el idioma, lo demás es nulo
        UpdateUserSettingsRequest request = new UpdateUserSettingsRequest("pt", null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserSettings result = updateUserSettingsUseCase.execute(userId, request);

        // Assert
        assertThat(result.getLanguage()).isEqualTo("pt");
        assertThat(result.getTheme()).isEqualTo("dark");
        assertThat(result.getNotifications()).isTrue();
        assertThat(result.getAutoplay()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar UserNotFoundError cuando el usuario no existe")
    void shouldThrowUserNotFoundErrorWhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UpdateUserSettingsRequest request = new UpdateUserSettingsRequest("en", "dark", true, true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateUserSettingsUseCase.execute(userId, request))
                .isInstanceOf(UserNotFoundError.class)
                .hasMessage("Usuario no encontrado");

        verify(userRepository, never()).save(any());
    }
}

