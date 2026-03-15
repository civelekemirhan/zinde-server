package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PollResponse {
    private Long id;
    private String title;
    private LocalDateTime expiresAt;
    private boolean expired;
    private int totalVotes;
    private Long votedOptionId;
    private List<PollOptionResponse> options;
}
