package com.wexec.zinde_server.dto.request;

import com.wexec.zinde_server.entity.TrainerSpecialty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CreateTrainerPackageRequest {

    @NotBlank
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @Min(1)
    private int durationDays;

    @Min(1)
    private int totalLessons;

    @Size(max = 10)
    private List<TrainerSpecialty> specialties;
}
