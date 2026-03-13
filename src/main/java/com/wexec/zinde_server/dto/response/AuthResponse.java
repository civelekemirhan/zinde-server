package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final UUID userId;
    private final String email;
    private final String username;
    private final String role;
    private final String roleDisplayName;
}
