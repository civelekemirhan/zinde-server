package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.TrainerApplicationResponse;
import com.wexec.zinde_server.entity.TrainerSpecialty;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trainer-applications")
@RequiredArgsConstructor
public class TrainerApplicationController {

    private final TrainerApplicationService applicationService;

    /** Antrenörlük başvurusu gönder */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrainerApplicationResponse>> apply(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("document") MultipartFile document,
            @RequestParam(value = "specializations", required = false) List<TrainerSpecialty> specializations) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.apply(principal.getId(), document, specializations)));
    }

    /** Kendi başvurusunun durumunu sorgula */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TrainerApplicationResponse>> getMyApplication(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getMyApplication(principal.getId())));
    }
}
