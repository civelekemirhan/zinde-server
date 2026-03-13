package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.ApplicationStatus;
import com.wexec.zinde_server.entity.TrainerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerApplicationRepository extends JpaRepository<TrainerApplication, Long> {

    Optional<TrainerApplication> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndStatus(UUID userId, ApplicationStatus status);

    List<TrainerApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status);
}
