package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(UUID userId);

    void deleteByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
