package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.CreateTrainerReviewRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.TrainerCardResponse;
import com.wexec.zinde_server.dto.response.TrainerReviewResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerCardService;
import com.wexec.zinde_server.service.TrainerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerDiscoveryController {

    private final TrainerCardService trainerCardService;
    private final TrainerReviewService trainerReviewService;

    /** Tüm antrenörleri kart formatında listele */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainerCardResponse>>> listTrainers() {
        return ResponseEntity.ok(ApiResponse.success(trainerCardService.listAll()));
    }

    /** Tek antrenörün kartı */
    @GetMapping("/{trainerId}/card")
    public ResponseEntity<ApiResponse<TrainerCardResponse>> getCard(
            @PathVariable UUID trainerId) {
        return ResponseEntity.ok(ApiResponse.success(trainerCardService.getCard(trainerId)));
    }

    /** Hero/kapak fotoğrafı yükle (sadece kendi kartı için) */
    @PatchMapping(value = "/me/hero-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrainerCardResponse>> uploadHeroImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerCardService.uploadHeroImage(principal.getId(), image)));
    }

    // ── Reviews ──────────────────────────────────────────────────────────────

    @PostMapping("/{trainerId}/reviews")
    public ResponseEntity<ApiResponse<TrainerReviewResponse>> addReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID trainerId,
            @Valid @RequestBody CreateTrainerReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerReviewService.create(principal.getId(), trainerId, req)));
    }

    @GetMapping("/{trainerId}/reviews")
    public ResponseEntity<ApiResponse<List<TrainerReviewResponse>>> getReviews(
            @PathVariable UUID trainerId) {
        return ResponseEntity.ok(ApiResponse.success(
                trainerReviewService.getByTrainer(trainerId)));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId) {
        trainerReviewService.delete(principal.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
