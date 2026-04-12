package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.TrainerCardResponse;
import com.wexec.zinde_server.entity.TrainerPackage;
import com.wexec.zinde_server.entity.TrainerProfile;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.entity.UserRole;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.TrainerPackageRepository;
import com.wexec.zinde_server.repository.TrainerProfileRepository;
import com.wexec.zinde_server.repository.TrainerReviewRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerCardService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository profileRepository;
    private final TrainerPackageRepository packageRepository;
    private final TrainerReviewRepository reviewRepository;
    private final StorageService storageService;

    public List<TrainerCardResponse> listAll() {
        return userRepository.findByRole(UserRole.TRAINER)
                .stream()
                .map(this::toCard)
                .collect(Collectors.toList());
    }

    public TrainerCardResponse getCard(UUID trainerId) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
        if (trainer.getRole() != UserRole.TRAINER) {
            throw new AppException("NOT_TRAINER", "Bu kullanıcı bir antrenör değil.", HttpStatus.BAD_REQUEST);
        }
        return toCard(trainer);
    }

    @Transactional
    public TrainerCardResponse uploadHeroImage(UUID trainerId, MultipartFile file) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
        if (trainer.getRole() != UserRole.TRAINER) {
            throw new AppException("NOT_TRAINER", "Sadece antrenörler hero fotoğrafı yükleyebilir.", HttpStatus.FORBIDDEN);
        }
        if (file == null || file.isEmpty()) {
            throw new AppException("FILE_REQUIRED", "Fotoğraf zorunludur.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel dosyaları yüklenebilir.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }

        byte[] resized = resizeHero(bytes);

        TrainerProfile profile = profileRepository.findByUserId(trainerId)
                .orElseGet(() -> TrainerProfile.builder().user(trainer).build());

        if (profile.getHeroImageKey() != null) {
            storageService.deleteFile(profile.getHeroImageKey());
        }

        String key = storageService.uploadFile(resized, "trainer-heroes", "image/jpeg");
        profile.setHeroImageKey(key);
        profileRepository.save(profile);

        return toCard(trainer);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private TrainerCardResponse toCard(User trainer) {
        Optional<TrainerProfile> profileOpt = profileRepository.findByUserId(trainer.getId());

        String specialty = profileOpt
                .map(TrainerProfile::getSpecializations)
                .filter(list -> list != null && !list.isEmpty())
                .map(list -> list.get(0).getDisplayName())
                .orElse(null);

        String heroImageUrl = profileOpt
                .map(TrainerProfile::getHeroImageKey)
                .filter(k -> k != null)
                .map(storageService::getPublicUrl)
                .orElse(null);

        String avatarUrl = trainer.getAvatarKey() != null
                ? storageService.getPublicUrl(trainer.getAvatarKey())
                : null;

        BigDecimal monthlyPrice = packageRepository
                .findFirstByTrainerIdAndActiveOrderByCreatedAtDesc(trainer.getId(), true)
                .map(TrainerPackage::getPrice)
                .orElse(null);

        double rating = reviewRepository.avgRatingByTrainerId(trainer.getId());
        long reviewCount = reviewRepository.countByTrainerId(trainer.getId());

        return TrainerCardResponse.builder()
                .id(trainer.getId())
                .name(trainer.getFirstName() + " " + trainer.getLastName())
                .specialty(specialty)
                .bio(trainer.getBio())
                .rating(Math.round(rating * 10.0) / 10.0)
                .reviewCount(reviewCount)
                .monthlyPrice(monthlyPrice)
                .heroImageUrl(heroImageUrl)
                .avatarUrl(avatarUrl)
                .build();
    }

    private byte[] resizeHero(byte[] imageBytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(1200, 400)
                    .crop(Positions.CENTER)
                    .outputFormat("jpeg")
                    .outputQuality(0.85)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException("IMAGE_PROCESS_ERROR", "Fotoğraf işlenemedi.");
        }
    }
}
