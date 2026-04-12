package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.TrainerPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerPackageRepository extends JpaRepository<TrainerPackage, Long> {

    List<TrainerPackage> findByTrainerIdAndActiveOrderByCreatedAtDesc(UUID trainerId, boolean active);

    Optional<TrainerPackage> findFirstByTrainerIdAndActiveOrderByCreatedAtDesc(UUID trainerId, boolean active);

    List<TrainerPackage> findByTrainerIdOrderByCreatedAtDesc(UUID trainerId);
}
