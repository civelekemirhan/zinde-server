package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.Conversation;
import com.wexec.zinde_server.entity.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            JOIN ConversationParticipant cp ON cp.conversation = c
            WHERE cp.user.id = :userId
            ORDER BY c.createdAt DESC
            """)
    List<Conversation> findByParticipantUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT cp.conversation FROM ConversationParticipant cp
            WHERE cp.conversation.type = :type
            GROUP BY cp.conversation
            HAVING COUNT(cp) = 2
              AND SUM(CASE WHEN cp.user.id IN :userIds THEN 1 ELSE 0 END) = 2
            """)
    Optional<Conversation> findDirectConversation(
            @Param("type") ConversationType type,
            @Param("userIds") List<UUID> userIds);
}
