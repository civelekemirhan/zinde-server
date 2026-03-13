package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.ReviewApplicationRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.TrainerApplicationResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trainer-applications")
@RequiredArgsConstructor
public class TrainerApplicationController {

    private final TrainerApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrainerApplicationResponse>> apply(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("document") MultipartFile document) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.apply(principal.getId(), document)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TrainerApplicationResponse>> getMyApplication(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getMyApplication(principal.getId())));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TrainerApplicationResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getPendingApplications()));
    }

    @PostMapping("/{applicationId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TrainerApplicationResponse>> review(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewApplicationRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.review(principal.getId(), applicationId, req)));
    }
}
