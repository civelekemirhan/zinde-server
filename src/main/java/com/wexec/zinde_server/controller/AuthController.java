package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.LoginRequest;
import com.wexec.zinde_server.dto.request.RefreshTokenRequest;
import com.wexec.zinde_server.dto.request.RegisterRequest;
import com.wexec.zinde_server.dto.request.VerifyOtpRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.AuthResponse;
import com.wexec.zinde_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyEmail(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request.getRefreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody Map<String, String> body) {
        authService.resendOtp(body.get("email"));
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
