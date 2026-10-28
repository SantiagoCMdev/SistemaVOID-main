package com.voidapp.infrastructure.http.controller;

import com.voidapp.domain.model.Comment;
import com.voidapp.domain.model.User;
import com.voidapp.infrastructure.repository.CommentRepository;
import com.voidapp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public record AddCommentRequest(String content) {}

    @GetMapping("/{videoId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable UUID videoId) {
        List<Comment> comments = commentRepository.findByVideoIdAndIsFlaggedFalseOrderByCreatedAtDesc(videoId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{videoId}")
    @SuppressWarnings("null")
    public ResponseEntity<Comment> addComment(
            @PathVariable UUID videoId,
            @RequestBody AddCommentRequest request,
            Authentication authentication) {
            
        UUID userId = (UUID) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow();
        
        Comment comment = Comment.builder()
                .user(user)
                .videoId(videoId)
                .content(request.content())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(commentRepository.save(comment));
    }
}
