package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    boolean existsByPollIdAndUserId(Long pollId, UUID userId);

    Optional<PollVote> findByPollIdAndUserId(Long pollId, UUID userId);

    List<PollVote> findByOptionId(Long optionId);

    List<PollVote> findByPollId(Long pollId);

    @Modifying
    @Query("DELETE FROM PollVote pv WHERE pv.poll.id = :pollId")
    void deleteByPollId(@Param("pollId") Long pollId);
}
