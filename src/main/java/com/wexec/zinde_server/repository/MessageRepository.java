package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
