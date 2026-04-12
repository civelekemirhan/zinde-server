package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PackageRateRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
