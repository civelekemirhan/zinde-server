package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.LoginRequest;
import com.wexec.zinde_server.dto.request.RegisterRequest;
import com.wexec.zinde_server.dto.response.AuthResponse;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${refresh.expiry-days:30}")
    private int refreshExpiryDays;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("EMAIL_TAKEN", "Bu e-posta adresi zaten kullanımda.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("USERNAME_TAKEN", "Bu kullanıcı adı zaten alınmış.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .role(UserRole.ATHLETE)
                .build();

        userRepository.save(user);
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

        refreshTokenRepository.delete(refreshToken);
        return buildAuthResponse(user);
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.deleteByToken(tokenValue);
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
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .gender(user.getGender().name())
                .role(user.getRole().name())
                .roleDisplayName(user.getRole().getDisplayName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
