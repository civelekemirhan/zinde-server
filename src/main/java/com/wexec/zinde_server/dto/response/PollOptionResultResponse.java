package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PollOptionResultResponse {
    private Long id;
    private String text;
    private int voteCount;
    private double percentage;
    private List<PollVoterResponse> voters;
}
