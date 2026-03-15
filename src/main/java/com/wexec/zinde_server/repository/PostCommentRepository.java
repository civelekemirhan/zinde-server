package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    // Üst seviye yorumlar (parent yoklar)
    List<PostComment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    // Bir yorumun yanıtları
    Page<PostComment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    // Silme için: bir yorumun tüm direkt yanıtları (rekürsif silmede kullanılır)
    List<PostComment> findByParentId(Long parentId);

    @Modifying
    @Query("DELETE FROM PostComment pc WHERE pc.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
