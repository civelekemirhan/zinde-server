package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.ModerationStatus;
import com.wexec.zinde_server.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByModerationStatusOrderByCreatedAtDesc(ModerationStatus status, Pageable pageable);

    Page<Post> findByUserIdAndModerationStatusOrderByCreatedAtDesc(UUID userId, ModerationStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.user.id IN :userIds
              AND p.moderationStatus = 'APPROVED'
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findFollowingFeed(@Param("userIds") Collection<UUID> userIds, Pageable pageable);

    long countByUserId(UUID userId);
}
