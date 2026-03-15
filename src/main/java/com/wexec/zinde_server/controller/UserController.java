package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.dto.response.UserProfileResponse;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.UserRepository;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getMyProfile(principal.getId())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getProfile(principal.getId(), userId)));
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getUserPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserPosts(principal.getId(), userId, page, size)));
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("avatar") MultipartFile avatar) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.uploadAvatar(principal.getId(), avatar)));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> removeAvatar(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.removeAvatar(principal.getId())));
    }

    @PutMapping("/me/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String token = body.get("fcmToken");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("FCM_TOKEN_REQUIRED", "FCM token boş olamaz."));
        }
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
        user.setFcmToken(token);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
