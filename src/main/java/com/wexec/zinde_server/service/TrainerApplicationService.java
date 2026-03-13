package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.ReviewApplicationRequest;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerApplicationService {

    private final TrainerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ModerationService moderationService;

    @Transactional
    public TrainerApplicationResponse apply(UUID userId, MultipartFile document) {
        if (applicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new AppException("APPLICATION_PENDING", "Zaten bekleyen bir başvurunuz var.");
        }

        User user = getUser(userId);

        if (document == null || document.isEmpty()) {
            throw new AppException("DOCUMENT_REQUIRED", "Belge zorunludur.");
        }
        String contentType = document.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel belgeler yüklenebilir.");
        }

        byte[] bytes;
        try {
            bytes = document.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Belge okunamadı.");
        }

        String documentKey = storageService.uploadFile(bytes, "trainer-docs", contentType);

        TrainerApplication application = applicationRepository.save(TrainerApplication.builder()
                .user(user)
                .documentKey(documentKey)
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

    public List<TrainerApplicationResponse> getPendingApplications() {
        return applicationRepository.findByStatusOrderByCreatedAtAsc(ApplicationStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TrainerApplicationResponse review(UUID adminId, Long applicationId, ReviewApplicationRequest req) {
        TrainerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "Başvuru bulunamadı.", HttpStatus.NOT_FOUND));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException("ALREADY_REVIEWED", "Bu başvuru zaten incelendi.");
        }

        User admin = getUser(adminId);
        application.setStatus(req.getApprove() ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        application.setModerationNote(req.getNote());
        application.setReviewedBy(admin);
        application.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(application);

        if (req.getApprove()) {
            User applicant = application.getUser();
            applicant.setRole(UserRole.TRAINER);
            userRepository.save(applicant);
        }

        return toResponse(application);
    }

    private TrainerApplicationResponse toResponse(TrainerApplication app) {
        return TrainerApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .username(app.getUser().getUsername())
                .documentUrl(storageService.getPublicUrl(app.getDocumentKey()))
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
