package com.voidapp.infrastructure.repository;

import com.voidapp.domain.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Favorite> findByUserIdAndVideoId(UUID userId, UUID videoId);
    void deleteByUserIdAndVideoId(UUID userId, UUID videoId);
}
