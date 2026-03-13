package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.CreateTrainerPackageRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.TrainerPackageResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trainer-packages")
@RequiredArgsConstructor
public class TrainerPackageController {

    private final TrainerPackageService packageService;

    @PostMapping
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTrainerPackageRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.create(principal.getId(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainerPackageResponse>>> getByTrainer(
            @RequestParam UUID trainerId) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.getByTrainer(trainerId)));
    }

    @PatchMapping("/{packageId}/toggle")
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> toggle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long packageId) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.toggleActive(principal.getId(), packageId)));
    }
}
