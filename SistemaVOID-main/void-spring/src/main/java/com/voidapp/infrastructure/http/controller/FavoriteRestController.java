package com.voidapp.infrastructure.http.controller;

import com.voidapp.domain.model.Favorite;
import com.voidapp.domain.model.Video;
import com.voidapp.infrastructure.repository.FavoriteRepository;
import com.voidapp.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteRestController {

    private final FavoriteRepository favoriteRepository;
    private final VideoRepository videoRepository;

    @GetMapping
    public ResponseEntity<List<Video>> getFavorites(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<Video> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(Favorite::getVideo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{videoId}")
    @Transactional
    @SuppressWarnings("null")
    public ResponseEntity<Favorite> addFavorite(@PathVariable UUID videoId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndVideoId(userId, videoId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        Video video = videoRepository.findById(videoId).orElseThrow();
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .video(video)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteRepository.save(favorite));
    }

    @DeleteMapping("/{videoId}")
    @Transactional
    public ResponseEntity<Void> removeFavorite(@PathVariable UUID videoId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        favoriteRepository.deleteByUserIdAndVideoId(userId, videoId);
        return ResponseEntity.ok().build();
    }
}
