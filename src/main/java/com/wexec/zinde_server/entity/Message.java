package com.wexec.zinde_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_conversation_created", columnList = "conversation_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType;

    /** TEXT ve TEXT_IMAGE için dolu, IMAGE ve AUDIO için null olabilir */
    @Column(length = 4000)
    private String content;

    /** IMAGE, AUDIO ve TEXT_IMAGE için Firebase Storage key */
    @Column(name = "media_key")
    private String mediaKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (messageType == null) messageType = MessageType.TEXT;
    }
}
