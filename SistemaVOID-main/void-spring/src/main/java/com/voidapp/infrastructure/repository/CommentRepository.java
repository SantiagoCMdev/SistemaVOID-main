package com.voidapp.infrastructure.repository;

import com.voidapp.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByVideoIdAndIsFlaggedFalseOrderByCreatedAtDesc(UUID videoId);
}
