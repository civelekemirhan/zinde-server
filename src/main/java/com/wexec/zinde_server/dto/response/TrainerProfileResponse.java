package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TrainerProfileResponse {
    private List<String> specializations;
    private Integer yearsOfExperience;
    private String city;
    /** null → sertifika henüz yüklenmedi */
    private ApplicationStatus certificateStatus;
}
