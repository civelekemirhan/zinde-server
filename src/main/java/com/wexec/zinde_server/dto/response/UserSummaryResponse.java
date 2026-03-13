package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
}
