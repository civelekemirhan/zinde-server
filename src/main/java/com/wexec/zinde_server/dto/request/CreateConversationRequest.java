package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateConversationRequest {

    @NotNull
    private UUID participantId;
}
