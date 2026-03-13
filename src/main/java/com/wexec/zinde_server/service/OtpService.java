package com.wexec.zinde_server.service;

import com.wexec.zinde_server.entity.OtpToken;
import com.wexec.zinde_server.repository.OtpTokenRepository;
import com.wexec.zinde_server.repository.PendingRegistrationRepository;
import com.wexec.zinde_server.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    @Value("${otp.expiry-minutes:3}")
    private int expiryMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendOtp(String email) {
        otpTokenRepository.deleteByEmail(email);

        String code = generateCode();

        OtpToken token = OtpToken.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        otpTokenRepository.save(token);
        emailService.sendOtpEmail(email, code);
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        Optional<OtpToken> tokenOpt = otpTokenRepository.findByEmailAndCodeAndUsedFalse(email, code);

        if (tokenOpt.isEmpty()) return false;

        OtpToken token = tokenOpt.get();
        if (token.isExpired()) {
            otpTokenRepository.delete(token);
            return false;
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
        return true;
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    // Suresi gecmis kayitlari her 5 dakikada bir temizle
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        otpTokenRepository.deleteByExpiresAtBefore(now);
        pendingRegistrationRepository.deleteByExpiresAtBefore(now);
        refreshTokenRepository.deleteByExpiresAtBefore(now);
    }
}
