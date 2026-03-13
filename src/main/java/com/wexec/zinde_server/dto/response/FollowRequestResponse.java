package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.FollowStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class FollowRequestResponse {
    private Long id;
    private UUID fromUserId;
    private String fromUsername;
    private UUID toUserId;
    private String toUsername;
    private FollowStatus status;
    private LocalDateTime createdAt;
}
