package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChatMessageResponse {
    private Long id;
    private UUID conversationId;
    private UUID senderId;
    private String senderUsername;
    private String senderFirstName;
    private String senderLastName;
    private String senderAvatarUrl;
    private MessageType messageType;
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
}
