package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.ApplicationStatus;
import com.wexec.zinde_server.entity.TrainerSpecialty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TrainerApplicationResponse {
    private Long id;
    private UUID userId;
    private String username;
    private String documentUrl;
    private List<TrainerSpecialty> specializations;
    private ApplicationStatus status;
    private String moderationNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
