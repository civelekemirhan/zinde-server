package com.wexec.zinde_server.repository;

import com.wexec.zinde_server.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
