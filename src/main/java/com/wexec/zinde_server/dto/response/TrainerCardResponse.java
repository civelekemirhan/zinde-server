package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class TrainerCardResponse {
    private UUID id;
    private String name;
    private String specialty;
    private String bio;
    private double rating;
    private long reviewCount;
    private BigDecimal monthlyPrice;
    private String heroImageUrl;
    private String avatarUrl;
}
