package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    List<PollOption> findByPollId(Long pollId);

    @Modifying
    @Query("DELETE FROM PollOption po WHERE po.poll.id = :pollId")
    void deleteByPollId(@Param("pollId") Long pollId);
}
