package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PollOptionResponse {
    private Long id;
    private String text;
    private int voteCount;
    private double percentage;
    private boolean votedByMe;
}
