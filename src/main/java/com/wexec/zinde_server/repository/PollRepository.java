package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollRepository extends JpaRepository<Poll, Long> {

    Optional<Poll> findByPostId(Long postId);

    @Query("SELECT p FROM Poll p JOIN p.post po JOIN po.user u WHERE u.id = :userId ORDER BY p.createdAt DESC")
    List<Poll> findByUserId(@Param("userId") UUID userId);
}
