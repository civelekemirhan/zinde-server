package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentionRepository extends JpaRepository<Mention, Long> {

    @Modifying
    @Query("DELETE FROM Mention m WHERE m.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM Mention m WHERE m.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
}
