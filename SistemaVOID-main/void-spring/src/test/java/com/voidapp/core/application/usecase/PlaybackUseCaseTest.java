package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.ConcurrentStreamLimitExceededError;
import com.voidapp.domain.model.Plan;
import com.voidapp.domain.model.PlaybackProgress;
import com.voidapp.domain.model.Subscription;
import com.voidapp.infrastructure.repository.PlaybackProgressRepository;
import com.voidapp.infrastructure.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaybackUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlaybackProgressRepository playbackProgressRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private PlaybackUseCase playbackUseCase;

    @Nested
    @DisplayName("authorizePlaybackSession")
    class AuthorizePlaybackSessionTests {

        @BeforeEach
        void setUp() {
            lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        }

        @Test
        @DisplayName("Debe autorizar sesión exitosamente cuando hay cupo disponible de pantallas simultáneas")
        void shouldAuthorizeSessionSuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            String sessionId = "sess_abc_123";

            Plan plan = Plan.builder()
                    .name("Plan Pro")
                    .maxConcurrentStreams(2)
                    .maxResolution("1080p")
                    .build();

            Subscription sub = Subscription.builder()
                    .userId(userId)
                    .plan(plan)
                    .status("ACTIVE")
                    .build();

            PlaybackUseCase.RegisterStreamSessionInput input =
                    new PlaybackUseCase.RegisterStreamSessionInput(userId, videoId, sessionId, "127.0.0.1", "Chrome");

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(sub));

            when(zSetOperations.zCard(anyString())).thenReturn(1L); // 1 stream activo, max es 2
            when(zSetOperations.score(anyString(), eq(sessionId))).thenReturn(null); // nueva sesión
            when(playbackProgressRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());

            // Act
            PlaybackUseCase.AuthorizationResponse response = playbackUseCase.authorizePlaybackSession(input);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.authorized()).isTrue();
            assertThat(response.resumePositionSeconds()).isEqualTo(0);
            assertThat(response.maxAllowedResolution()).isEqualTo("1080p");

            verify(zSetOperations).removeRangeByScore(eq("user:streams:" + userId), eq(0.0), anyDouble());
            verify(zSetOperations).add(eq("user:streams:" + userId), eq(sessionId), anyDouble());
            verify(redisTemplate).expire(eq("user:streams:" + userId), eq(120L), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("Debe retornar progreso previo de reproducción cuando el usuario ya había iniciado el video")
        void shouldReturnPreviousProgressWhenAvailable() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            String sessionId = "sess_abc_456";

            Plan plan = Plan.builder()
                    .name("Plan 4K")
                    .maxConcurrentStreams(4)
                    .maxResolution("4K")
                    .build();

            Subscription sub = Subscription.builder()
                    .userId(userId)
                    .plan(plan)
                    .build();

            PlaybackProgress existingProgress = PlaybackProgress.builder()
                    .userId(userId)
                    .videoId(videoId)
                    .lastPositionSeconds(350)
                    .totalDurationSeconds(1200)
                    .completed(false)
                    .build();

            PlaybackUseCase.RegisterStreamSessionInput input =
                    new PlaybackUseCase.RegisterStreamSessionInput(userId, videoId, sessionId, "127.0.0.1", "SmartTV");

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(sub));

            when(zSetOperations.zCard(anyString())).thenReturn(0L);
            when(zSetOperations.score(anyString(), eq(sessionId))).thenReturn(null);
            when(playbackProgressRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(existingProgress));

            // Act
            PlaybackUseCase.AuthorizationResponse response = playbackUseCase.authorizePlaybackSession(input);

            // Assert
            assertThat(response.authorized()).isTrue();
            assertThat(response.resumePositionSeconds()).isEqualTo(350);
            assertThat(response.maxAllowedResolution()).isEqualTo("4K");
        }

        @Test
        @DisplayName("Debe permitir continuar reproduciendo si la sesión ya existía aunque se alcance el límite")
        void shouldAllowExistingSessionEvenIfCountIsAtMax() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            String sessionId = "existing_session";

            Plan plan = Plan.builder()
                    .name("Plan Básico")
                    .maxConcurrentStreams(1)
                    .maxResolution("720p")
                    .build();

            Subscription sub = Subscription.builder().userId(userId).plan(plan).build();

            PlaybackUseCase.RegisterStreamSessionInput input =
                    new PlaybackUseCase.RegisterStreamSessionInput(userId, videoId, sessionId, "10.0.0.1", "Mobile");

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(sub));

            when(zSetOperations.zCard(anyString())).thenReturn(1L);
            when(zSetOperations.score(anyString(), eq(sessionId))).thenReturn(12345678.0); // Sesión existente
            when(playbackProgressRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());

            // Act
            PlaybackUseCase.AuthorizationResponse response = playbackUseCase.authorizePlaybackSession(input);

            // Assert
            assertThat(response.authorized()).isTrue();
            verify(zSetOperations).add(eq("user:streams:" + userId), eq(sessionId), anyDouble());
        }

        @Test
        @DisplayName("Debe lanzar ConcurrentStreamLimitExceededError cuando se supera el límite de pantallas simultáneas")
        void shouldThrowConcurrentStreamLimitExceededErrorWhenLimitReached() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            String newSessionId = "new_session_denied";

            Plan plan = Plan.builder()
                    .name("Plan Estándar")
                    .maxConcurrentStreams(2)
                    .maxResolution("1080p")
                    .build();

            Subscription sub = Subscription.builder().userId(userId).plan(plan).build();

            PlaybackUseCase.RegisterStreamSessionInput input =
                    new PlaybackUseCase.RegisterStreamSessionInput(userId, videoId, newSessionId, "127.0.0.1", "Web");

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(sub));

            when(zSetOperations.zCard(anyString())).thenReturn(2L); // Ya hay 2 sesiones activas
            when(zSetOperations.score(anyString(), eq(newSessionId))).thenReturn(null); // Es una nueva sesión

            // Act & Assert
            assertThatThrownBy(() -> playbackUseCase.authorizePlaybackSession(input))
                    .isInstanceOf(ConcurrentStreamLimitExceededError.class)
                    .hasMessageContaining("Límite alcanzado")
                    .hasMessageContaining("permite hasta 2 pantallas en simultáneo");

            verify(zSetOperations, never()).add(anyString(), eq(newSessionId), anyDouble());
            verify(playbackProgressRepository, never()).findByUserIdAndVideoId(any(), any());
        }
    }

    @Nested
    @DisplayName("syncProgress")
    class SyncProgressTests {

        @Test
        @DisplayName("Debe crear nuevo progreso de reproducción no completado")
        void shouldCreateNewProgressNotCompleted() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            PlaybackUseCase.SyncProgressInput input =
                    new PlaybackUseCase.SyncProgressInput(userId, videoId, 50, 100);

            when(playbackProgressRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());
            when(playbackProgressRepository.save(any(PlaybackProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlaybackProgress result = playbackUseCase.syncProgress(input);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getVideoId()).isEqualTo(videoId);
            assertThat(result.getLastPositionSeconds()).isEqualTo(50);
            assertThat(result.getTotalDurationSeconds()).isEqualTo(100);
            assertThat(result.getCompleted()).isFalse();

            verify(playbackProgressRepository).save(any(PlaybackProgress.class));
        }

        @Test
        @DisplayName("Debe marcar completed = true cuando la posición supera el 95% del total")
        void shouldMarkCompletedWhenPositionIsAtLeast95Percent() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            // 95 de 100 segundos = 95%
            PlaybackUseCase.SyncProgressInput input =
                    new PlaybackUseCase.SyncProgressInput(userId, videoId, 95, 100);

            when(playbackProgressRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());
            when(playbackProgressRepository.save(any(PlaybackProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlaybackProgress result = playbackUseCase.syncProgress(input);

            // Assert
            assertThat(result.getCompleted()).isTrue();
        }

        @Test
        @DisplayName("Debe actualizar progreso existente correctamente")
        void shouldUpdateExistingProgress() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID videoId = UUID.randomUUID();
            PlaybackProgress existing = PlaybackProgress.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .videoId(videoId)
                    .lastPositionSeconds(10)
                    .totalDurationSeconds(1000)
                    .completed(false)
                    .build();

            PlaybackUseCase.SyncProgressInput input =
                    new PlaybackUseCase.SyncProgressInput(userId, videoId, 500, 1000);

            when(playbackProgressRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(existing));
            when(playbackProgressRepository.save(existing)).thenReturn(existing);

            // Act
            PlaybackProgress result = playbackUseCase.syncProgress(input);

            // Assert
            assertThat(result.getLastPositionSeconds()).isEqualTo(500);
            assertThat(result.getCompleted()).isFalse();
            verify(playbackProgressRepository).save(existing);
        }
    }
}

