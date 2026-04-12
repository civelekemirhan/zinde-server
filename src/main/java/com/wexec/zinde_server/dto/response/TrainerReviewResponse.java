package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TrainerReviewResponse {
    private Long id;
    private UUID reviewerId;
    private String reviewerUsername;
    private String reviewerAvatarUrl;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
