package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.LoginRequest;
import com.wexec.zinde_server.dto.request.RegisterRequest;
import com.wexec.zinde_server.dto.request.VerifyOtpRequest;
import com.wexec.zinde_server.dto.response.AuthResponse;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.PendingRegistrationRepository;
import com.wexec.zinde_server.repository.RefreshTokenRepository;
import com.wexec.zinde_server.repository.UserRepository;
import com.wexec.zinde_server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${pending.expiry-minutes:30}")
    private int pendingExpiryMinutes;

    @Value("${refresh.expiry-days:30}")
    private int refreshExpiryDays;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("EMAIL_TAKEN", "Bu e-posta adresi zaten kullanımda.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("USERNAME_TAKEN", "Bu kullanıcı adı zaten alınmış.");
        }

        PendingRegistration pending = pendingRegistrationRepository
                .findByEmail(request.getEmail())
                .orElse(PendingRegistration.builder().build());

        if (pending.getUsername() == null || !pending.getUsername().equals(request.getUsername())) {
            if (pendingRegistrationRepository.existsByUsername(request.getUsername())) {
                throw new AppException("USERNAME_TAKEN", "Bu kullanıcı adı zaten alınmış.");
            }
        }

        pending.setEmail(request.getEmail());
        pending.setFirstName(request.getFirstName());
        pending.setLastName(request.getLastName());
        pending.setUsername(request.getUsername());
        pending.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        pending.setGender(request.getGender());
        pending.setRole(UserRole.ATHLETE);
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(pendingExpiryMinutes));

        pendingRegistrationRepository.save(pending);
        otpService.sendOtp(request.getEmail());
    }

    // ── Verify Email → kullanici olusturulur, token cifti döner ─────────────

    @Transactional
    public AuthResponse verifyEmail(VerifyOtpRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmail(), request.getCode());
        if (!valid) {
            throw new AppException("INVALID_OTP", "Geçersiz veya süresi dolmuş doğrulama kodu.");
        }

        PendingRegistration pending = pendingRegistrationRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("PENDING_NOT_FOUND",
                        "Kayıt bilgileri bulunamadı. Lütfen tekrar kayıt olun."));

        if (pending.isExpired()) {
            pendingRegistrationRepository.delete(pending);
            throw new AppException("PENDING_EXPIRED", "Kayıt süreniz dolmuş. Lütfen tekrar kayıt olun.");
        }

        User user = User.builder()
                .email(pending.getEmail())
                .firstName(pending.getFirstName())
                .lastName(pending.getLastName())
                .username(pending.getUsername())
                .passwordHash(pending.getPasswordHash())
                .gender(pending.getGender())
                .role(pending.getRole())
                .build();

        userRepository.save(user);
        pendingRegistrationRepository.delete(pending);

        return buildAuthResponse(user);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("INVALID_CREDENTIALS", "Geçersiz e-posta veya şifre."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException("INVALID_CREDENTIALS", "Geçersiz e-posta veya şifre.");
        }

        // Onceki refresh tokenları temizle (tek cihaz politikası degil, güvenlik icin)
        refreshTokenRepository.deleteByUserId(user.getId());

        return buildAuthResponse(user);
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(String tokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new AppException("INVALID_REFRESH_TOKEN",
                        "Geçersiz refresh token.", HttpStatus.UNAUTHORIZED));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException("REFRESH_TOKEN_EXPIRED",
                    "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.UNAUTHORIZED));

        // Token rotation: eskisini sil, yenisini olustur
        refreshTokenRepository.delete(refreshToken);

        return buildAuthResponse(user);
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.deleteByToken(tokenValue);
    }

    // ── OTP tekrar gonder ────────────────────────────────────────────────────

    @Transactional
    public void resendOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException("ALREADY_VERIFIED", "Bu hesap zaten doğrulanmış.");
        }
        pendingRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("PENDING_NOT_FOUND",
                        "Bu e-posta ile başlatılmış bir kayıt bulunamadı."));

        otpService.sendOtp(email);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpiryDays))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .roleDisplayName(user.getRole().getDisplayName())
                .build();
    }
}
