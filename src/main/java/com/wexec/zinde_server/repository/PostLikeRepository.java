package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.Post;
import com.wexec.zinde_server.entity.PostLike;
import com.wexec.zinde_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostIdAndUserId(Long postId, UUID userId);
}
