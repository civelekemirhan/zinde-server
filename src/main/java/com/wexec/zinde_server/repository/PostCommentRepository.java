package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Page<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);
}
