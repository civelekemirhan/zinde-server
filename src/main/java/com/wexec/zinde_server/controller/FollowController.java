package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.UserSummaryResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId) {
        followService.follow(principal.getId(), userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId) {
        followService.unfollow(principal.getId(), userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getFollowers(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.getFollowers(userId)));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getFollowing(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.getFollowing(userId)));
    }
}
