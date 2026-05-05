package com.wexec.zinde_server.dto.request;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiQuestionRequest {
    private String query;
    private List<ChatMessageDto> history = new ArrayList<>();
}
