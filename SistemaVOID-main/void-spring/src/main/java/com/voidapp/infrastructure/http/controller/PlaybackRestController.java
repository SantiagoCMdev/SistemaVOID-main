package com.voidapp.infrastructure.http.controller;

import com.voidapp.core.application.usecase.PlaybackUseCase;
import com.voidapp.domain.model.PlaybackProgress;
import com.voidapp.infrastructure.streaming.StreamTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/playback")
@RequiredArgsConstructor
public class PlaybackRestController {

    private final PlaybackUseCase playbackUseCase;
    private final StreamTokenService streamTokenService;

    public record AuthorizeRequest(UUID videoId, String sessionId, String ipAddress, String deviceInfo) {}
    public record SyncRequest(UUID videoId, Integer positionSeconds, Integer totalDurationSeconds) {}

    public record PlaybackAuthResponse(boolean authorized, int resumePositionSeconds, String maxAllowedResolution, String streamToken) {}

    @PostMapping("/authorize")
    public ResponseEntity<PlaybackAuthResponse> authorizeSession(@RequestBody AuthorizeRequest request, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        PlaybackUseCase.RegisterStreamSessionInput input = new PlaybackUseCase.RegisterStreamSessionInput(
                userId, request.videoId(), request.sessionId(), request.ipAddress(), request.deviceInfo()
        );

        PlaybackUseCase.AuthorizationResponse authRes = playbackUseCase.authorizePlaybackSession(input);

        StreamTokenService.StreamTokenPayload tokenPayload = streamTokenService.generateOneTimeToken(
                userId, request.videoId(), request.sessionId(), request.ipAddress()
        );

        return ResponseEntity.ok(new PlaybackAuthResponse(
                authRes.authorized(),
                authRes.resumePositionSeconds(),
                authRes.maxAllowedResolution(),
                tokenPayload.token()
        ));
    }

    @PostMapping("/sync")
    public ResponseEntity<PlaybackProgress> syncProgress(@RequestBody SyncRequest request, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        PlaybackUseCase.SyncProgressInput input = new PlaybackUseCase.SyncProgressInput(
                userId, request.videoId(), request.positionSeconds(), request.totalDurationSeconds()
        );

        PlaybackProgress progress = playbackUseCase.syncProgress(input);
        return ResponseEntity.ok(progress);
    }
}
