package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TrainerPackageResponse {
    private Long id;
    private UUID trainerId;
    private String trainerUsername;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationDays;
    private int totalLessons;
    private boolean active;
    private LocalDateTime createdAt;
}
