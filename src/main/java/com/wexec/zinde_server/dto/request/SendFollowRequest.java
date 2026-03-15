package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class SendFollowRequest {
    @NotNull
    private UUID toUserId;
}
