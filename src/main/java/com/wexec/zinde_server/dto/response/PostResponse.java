package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.ModerationStatus;
import com.wexec.zinde_server.entity.PostType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private UUID userId;
    private String username;
    private String avatarUrl;
    private PostType postType;
    private String imageUrl;
    private String caption;
    private ModerationStatus moderationStatus;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
    private PollResponse poll;
    private LocalDateTime createdAt;
}
