package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.CreateTrainerReviewRequest;
import com.wexec.zinde_server.dto.response.TrainerReviewResponse;
import com.wexec.zinde_server.entity.TrainerReview;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.entity.UserRole;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.TrainerReviewRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerReviewService {

    private final TrainerReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional
    public TrainerReviewResponse create(UUID reviewerId, UUID trainerId, CreateTrainerReviewRequest req) {
        User trainer = getUser(trainerId);
        if (trainer.getRole() != UserRole.TRAINER) {
            throw new AppException("NOT_TRAINER", "Bu kullanıcı bir antrenör değil.", HttpStatus.BAD_REQUEST);
        }
        if (reviewerId.equals(trainerId)) {
            throw new AppException("SELF_REVIEW", "Kendinizi değerlendiremezsiniz.", HttpStatus.BAD_REQUEST);
        }
        if (reviewRepository.existsByTrainerIdAndReviewerId(trainerId, reviewerId)) {
            throw new AppException("ALREADY_REVIEWED", "Bu antrenörü zaten değerlendirdiniz.", HttpStatus.CONFLICT);
        }

        User reviewer = getUser(reviewerId);
        TrainerReview review = reviewRepository.save(TrainerReview.builder()
                .trainer(trainer)
                .reviewer(reviewer)
                .rating(req.getRating())
                .comment(req.getComment())
                .build());

        return toResponse(review);
    }

    public List<TrainerReviewResponse> getByTrainer(UUID trainerId) {
        return reviewRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void delete(UUID reviewerId, Long reviewId) {
        TrainerReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException("REVIEW_NOT_FOUND", "Değerlendirme bulunamadı.", HttpStatus.NOT_FOUND));
        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new AppException("FORBIDDEN", "Bu değerlendirmeyi silme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }
        reviewRepository.delete(review);
    }

    private TrainerReviewResponse toResponse(TrainerReview r) {
        String avatarUrl = r.getReviewer().getAvatarKey() != null
                ? storageService.getPublicUrl(r.getReviewer().getAvatarKey())
                : null;
        return TrainerReviewResponse.builder()
                .id(r.getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerUsername(r.getReviewer().getUsername())
                .reviewerAvatarUrl(avatarUrl)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
