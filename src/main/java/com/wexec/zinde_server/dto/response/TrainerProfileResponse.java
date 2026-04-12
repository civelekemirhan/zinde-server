package com.wexec.zinde_server.dto.response;

import com.wexec.zinde_server.entity.ApplicationStatus;
import com.wexec.zinde_server.entity.TrainerSpecialty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TrainerProfileResponse {
    private List<TrainerSpecialty> specializations;
    private Integer yearsOfExperience;
    private String city;
    /** null → sertifika henüz yüklenmedi */
    private ApplicationStatus certificateStatus;
}
