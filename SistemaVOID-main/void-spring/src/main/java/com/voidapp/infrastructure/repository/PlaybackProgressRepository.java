package com.voidapp.infrastructure.repository;

import com.voidapp.domain.model.PlaybackProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaybackProgressRepository extends JpaRepository<PlaybackProgress, UUID> {
    Optional<PlaybackProgress> findByUserIdAndVideoId(UUID userId, UUID videoId);
}
