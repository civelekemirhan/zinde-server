package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.FcmTokenRequest;
import com.wexec.zinde_server.dto.request.UpdateProfileRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.PageResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.dto.response.UserProfileResponse;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.UserRepository;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(principal.getId(), request)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getProfile(principal.getId(), userId)));
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getUserPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                new PageResponse<>(userService.getUserPosts(principal.getId(), userId, page, size))));
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            @Valid @RequestBody FcmTokenRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
