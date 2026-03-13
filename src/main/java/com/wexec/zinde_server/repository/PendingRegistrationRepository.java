package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, UUID> {

    Optional<PendingRegistration> findByEmail(String email);

    boolean existsByUsername(String username);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
