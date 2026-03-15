package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {

    Optional<TrainerProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
