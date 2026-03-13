package com.wexec.zinde_server.service;

import com.wexec.zinde_server.entity.ApplicationStatus;
import com.wexec.zinde_server.entity.ModerationStatus;
import com.wexec.zinde_server.entity.Post;
import com.wexec.zinde_server.entity.TrainerApplication;
import com.wexec.zinde_server.repository.PostRepository;
import com.wexec.zinde_server.repository.TrainerApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;

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
    public void moderateDocument(TrainerApplication application, byte[] documentBytes) {
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

            if (hasExplicitContent) {
                application.setStatus(ApplicationStatus.REJECTED);
                application.setModerationNote("Belge uygunsuz içerik nedeniyle otomatik olarak reddedildi.");
                applicationRepository.save(application);
                log.info("TrainerApplication {} auto-rejected due to explicit content", application.getId());
            } else {
                log.info("TrainerApplication {} passed moderation, awaiting admin review", application.getId());
            }
        } catch (Exception e) {
            log.error("TrainerApplication {} document moderation failed", application.getId(), e);
        }
    }
}
