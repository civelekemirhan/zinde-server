package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PollVoterResponse {
    private UUID userId;
    private String username;
    private LocalDateTime votedAt;
}
