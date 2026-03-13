package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiError {

    private final String code;
    private final String message;
}
