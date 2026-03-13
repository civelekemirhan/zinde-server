package com.wexec.zinde_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponse {
    private final String message;
    private final boolean success;
}
