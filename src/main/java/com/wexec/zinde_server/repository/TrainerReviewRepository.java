package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.TrainerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerReviewRepository extends JpaRepository<TrainerReview, Long> {

    List<TrainerReview> findByTrainerIdOrderByCreatedAtDesc(UUID trainerId);

    boolean existsByTrainerIdAndReviewerId(UUID trainerId, UUID reviewerId);

    Optional<TrainerReview> findByTrainerIdAndReviewerId(UUID trainerId, UUID reviewerId);

    @Query("SELECT COUNT(r) FROM TrainerReview r WHERE r.trainer.id = :trainerId")
    long countByTrainerId(@Param("trainerId") UUID trainerId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM TrainerReview r WHERE r.trainer.id = :trainerId")
    double avgRatingByTrainerId(@Param("trainerId") UUID trainerId);
}
