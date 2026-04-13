package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.CreateTrainerPackageRequest;
import com.wexec.zinde_server.dto.request.PackageRateRequest;
import com.wexec.zinde_server.dto.response.PackagePurchaseSummaryResponse;
import com.wexec.zinde_server.dto.response.TrainerPackageResponse;
import com.wexec.zinde_server.entity.PackagePurchase;
import com.wexec.zinde_server.entity.PackageRating;
import com.wexec.zinde_server.entity.TrainerPackage;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.entity.UserRole;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.PackagePurchaseRepository;
import com.wexec.zinde_server.repository.PackageRatingRepository;
import com.wexec.zinde_server.repository.TrainerPackageRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerPackageService {

    private final TrainerPackageRepository packageRepository;
    private final PackagePurchaseRepository purchaseRepository;
    private final PackageRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // ── Admin: paket oluştur ──────────────────────────────────────────────────

    @Transactional
    public TrainerPackageResponse create(UUID trainerId, CreateTrainerPackageRequest req) {
        User trainer = getUser(trainerId);
        if (trainer.getRole() != UserRole.TRAINER) {
            throw new AppException("FORBIDDEN", "Sadece antrenörler paket oluşturabilir.", HttpStatus.FORBIDDEN);
        }

        TrainerPackage pkg = packageRepository.save(TrainerPackage.builder()
                .trainer(trainer)
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .durationDays(req.getDurationDays())
                .totalLessons(req.getTotalLessons())
                .specialties(req.getSpecialties() != null ? req.getSpecialties() : new java.util.ArrayList<>())
                .build());

        return toResponse(pkg);
    }

    // ── Admin: paket fotoğrafı yükle ─────────────────────────────────────────

    @Transactional
    public TrainerPackageResponse uploadImage(UUID trainerId, Long packageId, MultipartFile file) {
        TrainerPackage pkg = getPackage(packageId);
        if (!pkg.getTrainer().getId().equals(trainerId)) {
            throw new AppException("FORBIDDEN", "Bu paketi düzenleme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }
        validateImageFile(file);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }

        if (pkg.getImageKey() != null) {
            storageService.deleteFile(pkg.getImageKey());
        }

        String key = storageService.uploadFile(bytes, "package-images", file.getContentType());
        pkg.setImageKey(key);
        return toResponse(packageRepository.save(pkg));
    }

    // ── Admin: paket aktif/pasif ──────────────────────────────────────────────

    @Transactional
    public TrainerPackageResponse toggleActive(UUID trainerId, Long packageId) {
        TrainerPackage pkg = getPackage(packageId);
        if (!pkg.getTrainer().getId().equals(trainerId)) {
            throw new AppException("FORBIDDEN", "Bu paketi düzenleme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }
        pkg.setActive(!pkg.isActive());
        return toResponse(packageRepository.save(pkg));
    }

    // ── Listeleme ─────────────────────────────────────────────────────────────

    public List<TrainerPackageResponse> getByTrainer(UUID trainerId) {
        return packageRepository.findByTrainerIdAndActiveOrderByCreatedAtDesc(trainerId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TrainerPackageResponse> getMyPackages(UUID trainerId) {
        User trainer = getUser(trainerId);
        if (trainer.getRole() != UserRole.TRAINER) {
            throw new AppException("FORBIDDEN", "Sadece antrenörler bu endpoint'i kullanabilir.", HttpStatus.FORBIDDEN);
        }
        return packageRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TrainerPackageResponse getById(Long packageId) {
        return toResponse(getPackage(packageId));
    }

    public Page<TrainerPackageResponse> getAllActivePackages(String seed, int page, int size) {
        return packageRepository.findActiveWithSeed(seed, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    // ── Kullanıcı: paket satın al ─────────────────────────────────────────────

    @Transactional
    public PackagePurchaseSummaryResponse purchase(UUID userId, Long packageId) {
        TrainerPackage pkg = getPackage(packageId);
        if (!pkg.isActive()) {
            throw new AppException("PACKAGE_INACTIVE", "Bu paket aktif değil.", HttpStatus.BAD_REQUEST);
        }
        if (purchaseRepository.existsByUserIdAndTrainerPackageId(userId, packageId)) {
            throw new AppException("ALREADY_PURCHASED", "Bu paketi zaten satın aldınız.", HttpStatus.CONFLICT);
        }

        User user = getUser(userId);
        LocalDate now = LocalDate.now();

        PackagePurchase purchase = purchaseRepository.save(PackagePurchase.builder()
                .user(user)
                .trainerPackage(pkg)
                .purchasedAt(now)
                .expiresAt(now.plusDays(pkg.getDurationDays()))
                .build());

        return toPurchaseSummary(purchase);
    }

    // ── Kullanıcı: paketi oyla ────────────────────────────────────────────────

    @Transactional
    public TrainerPackageResponse rate(UUID userId, Long packageId, PackageRateRequest req) {
        if (!purchaseRepository.existsByUserIdAndTrainerPackageId(userId, packageId)) {
            throw new AppException("NOT_PURCHASED", "Bu paketi oylayabilmek için önce satın almalısınız.", HttpStatus.FORBIDDEN);
        }

        TrainerPackage pkg = getPackage(packageId);
        User user = getUser(userId);

        PackageRating rating = ratingRepository
                .findByUserIdAndTrainerPackageId(userId, packageId)
                .orElse(PackageRating.builder().user(user).trainerPackage(pkg).build());

        rating.setRating(req.getRating());
        ratingRepository.save(rating);

        return toResponse(pkg);
    }

    // ── Kullanıcı aktif paketleri ─────────────────────────────────────────────

    public List<PackagePurchaseSummaryResponse> getActivePurchases(UUID userId) {
        return purchaseRepository.findByUserIdOrderByPurchasedAtDesc(userId)
                .stream()
                .map(this::toPurchaseSummary)
                .collect(Collectors.toList());
    }

    // ── private helpers ───────────────────────────────────────────────────────

    public TrainerPackageResponse toResponse(TrainerPackage pkg) {
        String imageUrl = pkg.getImageKey() != null
                ? storageService.getPublicUrl(pkg.getImageKey())
                : null;
        double avg = ratingRepository.avgRatingByPackageId(pkg.getId());
        long count = ratingRepository.countByPackageId(pkg.getId());

        return TrainerPackageResponse.builder()
                .id(pkg.getId())
                .trainerId(pkg.getTrainer().getId())
                .trainerName(pkg.getTrainer().getFirstName() + " " + pkg.getTrainer().getLastName())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .durationDays(pkg.getDurationDays())
                .totalLessons(pkg.getTotalLessons())
                .specialties(pkg.getSpecialties())
                .imageUrl(imageUrl)
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .ratingCount(count)
                .active(pkg.isActive())
                .createdAt(pkg.getCreatedAt())
                .build();
    }

    public PackagePurchaseSummaryResponse toPurchaseSummaryPublic(PackagePurchase p) {
        return toPurchaseSummary(p);
    }

    private PackagePurchaseSummaryResponse toPurchaseSummary(PackagePurchase p) {
        String imageUrl = p.getTrainerPackage().getImageKey() != null
                ? storageService.getPublicUrl(p.getTrainerPackage().getImageKey())
                : null;
        return PackagePurchaseSummaryResponse.builder()
                .packageId(p.getTrainerPackage().getId())
                .packageName(p.getTrainerPackage().getName())
                .imageUrl(imageUrl)
                .price(p.getTrainerPackage().getPrice())
                .purchasedAt(p.getPurchasedAt())
                .expiresAt(p.getExpiresAt())
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("FILE_REQUIRED", "Fotoğraf zorunludur.");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel dosyaları yüklenebilir.");
        }
    }

    private TrainerPackage getPackage(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new AppException("PACKAGE_NOT_FOUND", "Paket bulunamadı.", HttpStatus.NOT_FOUND));
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
