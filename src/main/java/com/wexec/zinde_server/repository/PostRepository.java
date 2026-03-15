package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.ModerationStatus;
import com.wexec.zinde_server.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByModerationStatusOrderByCreatedAtDesc(ModerationStatus status, Pageable pageable);

    Page<Post> findByUserIdAndModerationStatusOrderByCreatedAtDesc(UUID userId, ModerationStatus status, Pageable pageable);

    long countByUserId(UUID userId);
}
