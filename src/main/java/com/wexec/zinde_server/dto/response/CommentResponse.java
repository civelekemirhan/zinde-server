package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private Long parentId;
    private UUID userId;
    private String username;
    private String avatarUrl;
    private String content;
    private List<CommentResponse> replies;
    private LocalDateTime createdAt;
}
