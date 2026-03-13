package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.ConversationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ConversationResponse {
    private UUID id;
    private ConversationType type;
    private String title;
    private List<UserSummaryResponse> participants;
    private ChatMessageResponse lastMessage;
    private LocalDateTime createdAt;
}
