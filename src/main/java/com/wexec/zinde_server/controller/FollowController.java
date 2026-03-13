package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.FollowRequestResponse;
import com.wexec.zinde_server.dto.response.UserSummaryResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<ApiResponse<FollowRequestResponse>> sendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        UUID toUserId = UUID.fromString(body.get("toUserId"));
        return ResponseEntity.ok(ApiResponse.success(
                followService.sendRequest(principal.getId(), toUserId)));
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<FollowRequestResponse>> accept(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.respond(principal.getId(), requestId, true)));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<FollowRequestResponse>> reject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.respond(principal.getId(), requestId, false)));
    }

    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse<List<FollowRequestResponse>>> getIncoming(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.getIncomingRequests(principal.getId())));
    }

    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getFriends(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                followService.getFriends(principal.getId())));
    }
}
