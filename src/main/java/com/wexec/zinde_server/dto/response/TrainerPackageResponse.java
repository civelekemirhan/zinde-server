package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.TrainerSpecialty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TrainerPackageResponse {
    private Long id;
    private UUID trainerId;
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private String trainerAvatarUrl;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationDays;
    private int totalLessons;
    private List<TrainerSpecialty> specialties;
    private String imageUrl;
    private double averageRating;
    private long ratingCount;
    private boolean active;
    private LocalDateTime createdAt;
}
