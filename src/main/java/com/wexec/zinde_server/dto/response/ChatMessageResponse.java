package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChatMessageResponse {
    private Long id;
    private UUID senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime createdAt;
}
