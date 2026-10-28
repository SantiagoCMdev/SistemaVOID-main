package com.voidapp.infrastructure.http.controller;

import com.voidapp.domain.model.Video;
import com.voidapp.infrastructure.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoRestController {

    private final VideoRepository videoRepository;

    @GetMapping
    public ResponseEntity<List<Video>> getCatalog(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String q) {

        // For simplicity, we just return all videos. A Specification could be added for exact filtering.
        List<Video> videos = videoRepository.findAll();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Video> getVideoBySlug(@PathVariable String slug) {
        return videoRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
