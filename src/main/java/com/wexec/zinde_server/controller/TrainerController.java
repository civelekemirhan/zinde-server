package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.TrainerProfileRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.TrainerProfileResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    /** Antrenör profilini güncelle (uzmanlıklar, şehir, deneyim) */
    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> upsertProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TrainerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerService.upsertProfile(principal.getId(), request)));
    }

    /** Kendi antrenör profilini getir */
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerService.getMyProfile(principal.getId())));
    }
}
