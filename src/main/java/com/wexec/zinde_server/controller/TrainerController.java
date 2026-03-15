package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.TrainerProfileRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.TrainerProfileResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    /**
     * Antrenör profilini oluşturur veya günceller.
     * Sadece role=TRAINER olan kullanıcılar erişebilir.
     *
     * Body (JSON):
     *   specializations  → ["Fitness", "Pilates"]  (isteğe bağlı)
     *   yearsOfExperience → 5                       (isteğe bağlı)
     *   city             → "İstanbul"               (isteğe bağlı)
     */
    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> upsertProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TrainerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerService.upsertProfile(principal.getId(), request)));
    }

    /**
     * Sertifika/belge yükleyerek antrenörlük başvurusu oluşturur.
     * Dosya: PDF veya görsel (jpg, png, vb.)
     * Bekleyen aktif başvuru varsa hata döner.
     */
    @PostMapping(value = "/me/certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> submitCertificate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("document") MultipartFile document) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerService.submitCertificate(principal.getId(), document)));
    }

    /**
     * Antrenör profilini ve sertifika durumunu döner.
     */
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerService.getMyProfile(principal.getId())));
    }
}
