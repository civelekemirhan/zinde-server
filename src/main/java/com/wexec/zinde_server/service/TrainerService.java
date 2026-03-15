package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.TrainerProfileRequest;
import com.wexec.zinde_server.dto.response.TrainerProfileResponse;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.TrainerApplicationRepository;
import com.wexec.zinde_server.repository.TrainerProfileRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final TrainerApplicationRepository trainerApplicationRepository;
    private final StorageService storageService;

    @Transactional
    public TrainerProfileResponse upsertProfile(UUID userId, TrainerProfileRequest request) {
        User user = getTrainer(userId);

        TrainerProfile profile = trainerProfileRepository.findByUserId(userId)
                .orElseGet(() -> TrainerProfile.builder().user(user).build());

        if (request.getSpecializations() != null) {
            profile.setSpecializations(String.join(",", request.getSpecializations()));
        }
        if (request.getYearsOfExperience() != null) {
            profile.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }

        trainerProfileRepository.save(profile);
        return toResponse(profile, userId);
    }

    @Transactional
    public TrainerProfileResponse submitCertificate(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("FILE_REQUIRED", "Sertifika belgesi zorunludur.");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece PDF veya görsel dosyası yüklenebilir.");
        }
        if (trainerApplicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new AppException("APPLICATION_PENDING", "Zaten inceleme bekleyen bir başvurunuz var.");
        }

        User user = getTrainer(userId);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }

        String docKey = storageService.uploadFile(bytes, "trainer-certificates", contentType);

        trainerApplicationRepository.save(TrainerApplication.builder()
                .user(user)
                .documentKey(docKey)
                .status(ApplicationStatus.PENDING)
                .build());

        TrainerProfile profile = trainerProfileRepository.findByUserId(userId)
                .orElseGet(() -> TrainerProfile.builder().user(user).build());
        trainerProfileRepository.save(profile);

        return toResponse(profile, userId);
    }

    public TrainerProfileResponse getMyProfile(UUID userId) {
        getTrainer(userId);
        TrainerProfile profile = trainerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    return TrainerProfile.builder().user(user).build();
                });
        return toResponse(profile, userId);
    }

    // ── Yardımcı ────────────────────────────────────────────────────────────

    private TrainerProfileResponse toResponse(TrainerProfile profile, UUID userId) {
        ApplicationStatus certStatus = trainerApplicationRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(TrainerApplication::getStatus)
                .orElse(null);

        return TrainerProfileResponse.builder()
                .specializations(parseSpecializations(profile.getSpecializations()))
                .yearsOfExperience(profile.getYearsOfExperience())
                .city(profile.getCity())
                .certificateStatus(certStatus)
                .build();
    }

    private List<String> parseSpecializations(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        return Arrays.asList(raw.split(","));
    }

    private User getTrainer(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
        if (user.getRole() != UserRole.TRAINER) {
            throw new AppException("NOT_TRAINER", "Bu işlem sadece antrenörler tarafından yapılabilir.", HttpStatus.FORBIDDEN);
        }
        return user;
    }
}
