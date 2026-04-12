package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PackagePurchaseSummaryResponse {
    private Long packageId;
    private String packageName;
    private String imageUrl;
    private BigDecimal price;
    private LocalDate purchasedAt;
    private LocalDate expiresAt;
}
