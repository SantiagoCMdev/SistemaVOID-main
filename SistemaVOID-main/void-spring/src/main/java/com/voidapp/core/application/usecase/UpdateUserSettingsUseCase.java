package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserSettings;
import com.voidapp.infrastructure.http.dto.UpdateUserSettingsRequest;
import com.voidapp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserSettingsUseCase {

    private final UserRepository userRepository;

    @Transactional
    @SuppressWarnings("null")
    public UserSettings execute(UUID userId, UpdateUserSettingsRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundError("Usuario no encontrado"));

        UserSettings settings = user.getSettings();
        if (settings == null) {
            settings = UserSettings.builder().user(user).build();
            user.setSettings(settings);
        }

        if (dto.language() != null) settings.setLanguage(dto.language());
        if (dto.theme() != null) settings.setTheme(dto.theme());
        if (dto.notifications() != null) settings.setNotifications(dto.notifications());
        if (dto.autoplay() != null) settings.setAutoplay(dto.autoplay());

        userRepository.save(user);

        return user.getSettings();
    }
}
