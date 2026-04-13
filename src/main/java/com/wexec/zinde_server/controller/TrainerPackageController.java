package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.CreateTrainerPackageRequest;
import com.wexec.zinde_server.dto.request.PackageRateRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.PackagePurchaseSummaryResponse;
import com.wexec.zinde_server.dto.response.PageResponse;
import com.wexec.zinde_server.dto.response.TrainerPackageResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.TrainerPackageService;
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
@RequestMapping("/api/trainer-packages")
@RequiredArgsConstructor
public class TrainerPackageController {

    private final TrainerPackageService packageService;

    // ── Admin endpoint'leri ───────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTrainerPackageRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.create(principal.getId(), req)));
    }

    @PatchMapping(value = "/{packageId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> uploadImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long packageId,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.uploadImage(principal.getId(), packageId, image)));
    }

    @PatchMapping("/{packageId}/toggle")
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> toggle(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long packageId) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.toggleActive(principal.getId(), packageId)));
    }

    // ── Public listeleme ──────────────────────────────────────────────────────

    /**
     * Tüm trainerların aktif paketleri (pagination + seed ile randomize sıra).
     * İlk açılışta client rastgele bir seed üretir, sayfalama boyunca aynı seed'i gönderir.
     * Farklı seed → farklı sıralama.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<TrainerPackageResponse>>> getAllActive(
            @RequestParam String seed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                new PageResponse<>(packageService.getAllActivePackages(seed, page, size))));
    }

    /** Belirtilen trainer'ın aktif paketlerini listeler */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainerPackageResponse>>> getByTrainer(
            @RequestParam UUID trainerId) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.getByTrainer(trainerId)));
    }

    /** Oturum açmış trainer'ın tüm paketlerini listeler (aktif + pasif) */
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<TrainerPackageResponse>>> getMyPackages(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.getMyPackages(principal.getId())));
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> getById(
            @PathVariable Long packageId) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.getById(packageId)));
    }

    // ── Kullanıcı: satın alma & oylama ────────────────────────────────────────

    @PostMapping("/{packageId}/purchase")
    public ResponseEntity<ApiResponse<PackagePurchaseSummaryResponse>> purchase(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long packageId) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.purchase(principal.getId(), packageId)));
    }

    @PostMapping("/{packageId}/rate")
    public ResponseEntity<ApiResponse<TrainerPackageResponse>> rate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long packageId,
            @Valid @RequestBody PackageRateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                packageService.rate(principal.getId(), packageId, req)));
    }
}
