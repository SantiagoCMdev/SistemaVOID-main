package com.voidapp.infrastructure.http.dto;

public record UpdateUserSettingsRequest(
        String language,
        String theme,
        Boolean notifications,
        Boolean autoplay
) {}
