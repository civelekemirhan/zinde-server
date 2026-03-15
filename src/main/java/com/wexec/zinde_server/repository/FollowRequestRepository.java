package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.FollowRequest;
import com.wexec.zinde_server.entity.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    Optional<FollowRequest> findByFromUserIdAndToUserId(UUID fromId, UUID toId);

    boolean existsByFromUserIdAndToUserId(UUID fromId, UUID toId);

    List<FollowRequest> findByToUserIdAndStatus(UUID toUserId, FollowStatus status);

    List<FollowRequest> findByFromUserIdAndStatus(UUID fromUserId, FollowStatus status);

    @Query("""
            SELECT fr FROM FollowRequest fr
            WHERE fr.status = 'ACCEPTED'
              AND (fr.fromUser.id = :userId OR fr.toUser.id = :userId)
            """)
    List<FollowRequest> findFriends(@Param("userId") UUID userId);

    @Query("""
            SELECT COUNT(fr) > 0 FROM FollowRequest fr
            WHERE fr.status = 'ACCEPTED'
              AND ((fr.fromUser.id = :a AND fr.toUser.id = :b)
                OR (fr.fromUser.id = :b AND fr.toUser.id = :a))
            """)
    boolean areFriends(@Param("a") UUID a, @Param("b") UUID b);
}
