package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.ConcurrentStreamLimitExceededError;
import com.voidapp.domain.model.Plan;
import com.voidapp.domain.model.PlaybackProgress;
import com.voidapp.domain.model.Subscription;
import com.voidapp.infrastructure.repository.PlaybackProgressRepository;
import com.voidapp.infrastructure.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PlaybackUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final PlaybackProgressRepository playbackProgressRepository;
    private final StringRedisTemplate redisTemplate;

    public record RegisterStreamSessionInput(UUID userId, UUID videoId, String sessionId, String ipAddress, String deviceInfo) {}
    public record SyncProgressInput(UUID userId, UUID videoId, Integer positionSeconds, Integer totalDurationSeconds) {}

    public record AuthorizationResponse(boolean authorized, int resumePositionSeconds, String maxAllowedResolution) {}

    public AuthorizationResponse authorizePlaybackSession(RegisterStreamSessionInput input) {
        // 1. Validar suscripción activa (Bypass temporal, idéntico al TS)
        Subscription activeSub = subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                input.userId(), "ACTIVE", ZonedDateTime.now()
        ).orElseGet(() -> {
            Plan mockPlan = Plan.builder()
                    .name("Plan Premium (Temporal)")
                    .maxConcurrentStreams(4)
                    .maxResolution("4K")
                    .build();
            return Subscription.builder()
                    .plan(mockPlan)
                    .build();
        });

        int maxConcurrent = activeSub.getPlan().getMaxConcurrentStreams();
        String activeSessionsKey = "user:streams:" + input.userId();
        
        long now = System.currentTimeMillis();
        long heartbeatCutoff = now - (60 * 1000); // 60 segundos
        
        // 2. Limpiar sesiones inactivas
        redisTemplate.opsForZSet().removeRangeByScore(activeSessionsKey, 0, heartbeatCutoff);
        
        // 3. Contar streams activos
        Long currentStreamCount = redisTemplate.opsForZSet().zCard(activeSessionsKey);
        if (currentStreamCount == null) currentStreamCount = 0L;
        
        Double sessionScore = redisTemplate.opsForZSet().score(activeSessionsKey, Objects.requireNonNull(input.sessionId()));
        
        if (sessionScore == null && currentStreamCount >= maxConcurrent) {
            throw new ConcurrentStreamLimitExceededError(
                    "Límite alcanzado: Tu plan \"" + activeSub.getPlan().getName() + "\" permite hasta " + maxConcurrent + " pantallas en simultáneo."
            );
        }
        
        // 4. Registrar heartbeat
        redisTemplate.opsForZSet().add(activeSessionsKey, Objects.requireNonNull(input.sessionId()), now);
        redisTemplate.expire(activeSessionsKey, 120, TimeUnit.SECONDS);

        // 5. Último progreso
        PlaybackProgress progress = playbackProgressRepository.findByUserIdAndVideoId(input.userId(), input.videoId())
                .orElse(null);
                
        return new AuthorizationResponse(
                true,
                progress != null ? progress.getLastPositionSeconds() : 0,
                activeSub.getPlan().getMaxResolution()
        );
    }

    @Transactional
    public PlaybackProgress syncProgress(SyncProgressInput input) {
        boolean isCompleted = input.positionSeconds() >= (input.totalDurationSeconds() * 0.95);
        
        PlaybackProgress progress = playbackProgressRepository.findByUserIdAndVideoId(input.userId(), input.videoId())
                .orElseGet(() -> PlaybackProgress.builder()
                        .userId(input.userId())
                        .videoId(input.videoId())
                        .build());
                        
        progress.setLastPositionSeconds(input.positionSeconds());
        progress.setTotalDurationSeconds(input.totalDurationSeconds());
        progress.setCompleted(isCompleted);
        
        return playbackProgressRepository.save(Objects.requireNonNull(progress));
    }
}
