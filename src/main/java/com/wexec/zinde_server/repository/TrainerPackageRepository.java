package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.TrainerPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerPackageRepository extends JpaRepository<TrainerPackage, Long> {

    List<TrainerPackage> findByTrainerIdAndActiveOrderByCreatedAtDesc(UUID trainerId, boolean active);

    Optional<TrainerPackage> findFirstByTrainerIdAndActiveOrderByCreatedAtDesc(UUID trainerId, boolean active);

    List<TrainerPackage> findByTrainerIdOrderByCreatedAtDesc(UUID trainerId);

    Page<TrainerPackage> findByActiveOrderByCreatedAtDesc(boolean active, Pageable pageable);

    @Query(
        value = "SELECT * FROM trainer_packages WHERE active = true ORDER BY md5(:seed || CAST(id AS text))",
        countQuery = "SELECT COUNT(*) FROM trainer_packages WHERE active = true",
        nativeQuery = true
    )
    Page<TrainerPackage> findActiveWithSeed(@Param("seed") String seed, Pageable pageable);
}
