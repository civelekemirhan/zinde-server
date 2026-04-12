package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    Optional<FollowRequest> findByFromUserIdAndToUserId(UUID fromId, UUID toId);

    boolean existsByFromUserIdAndToUserId(UUID fromId, UUID toId);

    /** Beni takip edenler (followers) */
    List<FollowRequest> findByToUserId(UUID toUserId);

    /** Benim takip ettiklerim (following) */
    List<FollowRequest> findByFromUserId(UUID fromUserId);

    long countByToUserId(UUID toUserId);

    long countByFromUserId(UUID fromUserId);
}
