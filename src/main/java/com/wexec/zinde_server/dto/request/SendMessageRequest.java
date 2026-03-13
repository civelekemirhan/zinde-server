package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SendMessageRequest {

    @NotNull
    private UUID conversationId;

    @NotBlank
    @Size(max = 4000)
    private String content;
}
