package com.wexec.zinde_server.service;

import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.repository.PostRepository;
import com.wexec.zinde_server.repository.TrainerApplicationRepository;
import com.wexec.zinde_server.repository.TrainerProfileRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    private static final Set<String> BLOCKED_LABELS = Set.of(
            "Explicit Nudity", "Nudity", "Graphic Male Nudity", "Graphic Female Nudity",
            "Sexual Activity", "Illustrated Explicit Nudity", "Adult Content"
    );
    private static final float CONFIDENCE_THRESHOLD = 75.0f;

    private final RekognitionClient rekognitionClient;
    private final PostRepository postRepository;
    private final TrainerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    @Async
    public void moderate(Post post, byte[] imageBytes) {
        try {
            DetectModerationLabelsRequest request = DetectModerationLabelsRequest.builder()
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .minConfidence(CONFIDENCE_THRESHOLD)
                    .build();

            DetectModerationLabelsResponse response = rekognitionClient.detectModerationLabels(request);

            boolean rejected = response.moderationLabels().stream()
                    .map(ModerationLabel::name)
                    .anyMatch(BLOCKED_LABELS::contains);

            post.setModerationStatus(rejected ? ModerationStatus.REJECTED : ModerationStatus.APPROVED);
            postRepository.save(post);

            log.info("Post {} moderation result: {}", post.getId(), post.getModerationStatus());
        } catch (Exception e) {
            log.error("Post {} moderation failed, approving by default", post.getId(), e);
            post.setModerationStatus(ModerationStatus.APPROVED);
            postRepository.save(post);
        }
    }

    @Async
    @Transactional
    public void moderateDocument(TrainerApplication application, byte[] documentBytes) {
        // PDF'leri Rekognition işleyemez — direkt onayla
        String docKey = application.getDocumentKey();
        boolean isPdf = docKey != null && docKey.endsWith(".pdf");

        boolean approved;

        if (isPdf) {
            approved = true;
        } else {
            try {
                DetectModerationLabelsRequest request = DetectModerationLabelsRequest.builder()
                        .image(Image.builder()
                                .bytes(SdkBytes.fromByteArray(documentBytes))
                                .build())
                        .minConfidence(CONFIDENCE_THRESHOLD)
                        .build();

                DetectModerationLabelsResponse response = rekognitionClient.detectModerationLabels(request);

                boolean hasExplicitContent = response.moderationLabels().stream()
                        .map(ModerationLabel::name)
                        .anyMatch(BLOCKED_LABELS::contains);

                approved = !hasExplicitContent;
            } catch (Exception e) {
                log.error("TrainerApplication {} moderation failed, approving by default", application.getId(), e);
                approved = true;
            }
        }

        if (approved) {
            application.setStatus(ApplicationStatus.APPROVED);
            application.setReviewedAt(LocalDateTime.now());
            applicationRepository.save(application);

            User user = application.getUser();
            user.setRole(UserRole.TRAINER);
            userRepository.save(user);

            TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                    .orElse(TrainerProfile.builder().user(user).build());
            profile.setSpecializations(application.getSpecializations());
            trainerProfileRepository.save(profile);

            log.info("TrainerApplication {} auto-approved, user {} promoted to TRAINER",
                    application.getId(), user.getId());
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setModerationNote("Belge uygunsuz içerik nedeniyle otomatik olarak reddedildi.");
            application.setReviewedAt(LocalDateTime.now());
            applicationRepository.save(application);
            log.info("TrainerApplication {} auto-rejected due to explicit content", application.getId());
        }
    }
}
