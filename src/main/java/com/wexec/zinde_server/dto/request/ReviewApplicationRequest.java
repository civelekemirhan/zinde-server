package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewApplicationRequest {

    @NotNull
    private Boolean approve;

    private String note;
}
