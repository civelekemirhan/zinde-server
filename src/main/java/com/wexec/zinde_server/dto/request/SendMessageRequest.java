package com.wexec.zinde_server.dto.request;

import com.wexec.zinde_server.entity.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SendMessageRequest {

    @NotNull
    private UUID conversationId;

    @NotNull
    private MessageType messageType;

    /** TEXT ve TEXT_IMAGE için dolu olmalı */
    @Size(max = 4000)
    private String content;

    /**
     * IMAGE, AUDIO ve TEXT_IMAGE için dolu olmalı.
     * Önce REST ile medyayı yükle, dönen mediaKey'i buraya gönder.
     */
    private String mediaKey;
}
