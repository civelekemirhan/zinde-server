package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.TrainerApplicationResponse;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.TrainerApplicationRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainerApplicationService {

    private final TrainerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ModerationService moderationService;

    @Transactional
    public TrainerApplicationResponse apply(UUID userId, MultipartFile document,
                                            List<TrainerSpecialty> specializations) {
        if (applicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new AppException("APPLICATION_PENDING", "Zaten bekleyen bir başvurunuz var.");
        }

        User user = getUser(userId);

        if (document == null || document.isEmpty()) {
            throw new AppException("DOCUMENT_REQUIRED", "Antrenörlük belgesi zorunludur.");
        }
        String contentType = document.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel veya PDF belge yüklenebilir.");
        }

        byte[] bytes;
        try {
            bytes = document.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Belge okunamadı.");
        }

        String documentKey = storageService.uploadFile(bytes, "trainer-docs", contentType);

        List<TrainerSpecialty> specs = (specializations != null) ? specializations : Collections.emptyList();

        TrainerApplication application = applicationRepository.save(TrainerApplication.builder()
                .user(user)
                .documentKey(documentKey)
                .specializations(specs)
                .status(ApplicationStatus.PENDING)
                .build());

        moderationService.moderateDocument(application, bytes);

        return toResponse(application);
    }

    public TrainerApplicationResponse getMyApplication(UUID userId) {
        TrainerApplication application = applicationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Başvuru bulunamadı.", HttpStatus.NOT_FOUND));
        return toResponse(application);
    }

    private TrainerApplicationResponse toResponse(TrainerApplication app) {
        return TrainerApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .username(app.getUser().getUsername())
                .documentUrl(storageService.getPublicUrl(app.getDocumentKey()))
                .specializations(app.getSpecializations())
                .status(app.getStatus())
                .moderationNote(app.getModerationNote())
                .createdAt(app.getCreatedAt())
                .reviewedAt(app.getReviewedAt())
                .build();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
