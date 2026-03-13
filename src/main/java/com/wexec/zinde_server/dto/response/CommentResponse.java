package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private UUID userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
}
