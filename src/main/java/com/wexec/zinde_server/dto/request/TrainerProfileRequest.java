package com.wexec.zinde_server.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainerProfileRequest {

    @Size(max = 10, message = "En fazla 10 uzmanlık alanı girilebilir.")
    private List<@Size(max = 50) String> specializations;

    @Min(0)
    @Max(60)
    private Integer yearsOfExperience;

    @Size(max = 100)
    private String city;
}
