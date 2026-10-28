package com.voidapp.infrastructure.config;

import com.voidapp.domain.model.Plan;
import com.voidapp.domain.model.User;
import com.voidapp.domain.model.UserRole;
import com.voidapp.domain.model.Video;
import com.voidapp.infrastructure.repository.PlanRepository;
import com.voidapp.infrastructure.repository.UserRepository;
import com.voidapp.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final VideoRepository videoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Inicializando datos de prueba en la base de datos...");

            User admin = User.builder()
                    .fullName("Administrador VOID")
                    .email("admin@voidapp.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .build();

            User user = User.builder()
                    .fullName("Usuario Demo")
                    .email("demo@voidapp.com")
                    .passwordHash(passwordEncoder.encode("demo123"))
                    .role(UserRole.USER)
                    .build();

            userRepository.saveAll(List.of(admin, user));
            log.info("Usuarios creados: admin@voidapp.com (admin123), demo@voidapp.com (demo123)");
        }

        if (planRepository.count() == 0) {
            Plan standardPlan = Plan.builder()
                    .name("Plan Estándar")
                    .priceCents(999)
                    .maxConcurrentStreams(2)
                    .maxResolution("1080p")
                    .isActive(true)
                    .currency("USD")
                    .build();

            Plan premiumPlan = Plan.builder()
                    .name("Plan Premium 4K")
                    .priceCents(1499)
                    .maxConcurrentStreams(4)
                    .maxResolution("4K")
                    .isActive(true)
                    .currency("USD")
                    .build();

            planRepository.saveAll(List.of(standardPlan, premiumPlan));
            log.info("Planes creados: Estándar y Premium");
        }

        if (videoRepository.count() == 0) {
            Video video = Video.builder()
                    .title("Inception (El Origen)")
                    .slug("inception-el-origen")
                    .description("Un ladrón que roba secretos corporativos mediante el uso de la tecnología de compartir sueños.")
                    .durationSeconds(8880)
                    .releaseYear(2010)
                    .ageRating("PG-13")
                    .thumbnailUrl("https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500")
                    .posterUrl("https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800")
                    .hlsMasterPlaylistUrl("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8")
                    .isPublished(true)
                    .build();

            videoRepository.save(video);
            log.info("Video de prueba creado: Inception");
        }
    }
}
