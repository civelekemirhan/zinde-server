package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.VoteRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.PollResponse;
import com.wexec.zinde_server.dto.response.PollResultResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<ApiResponse<PollResponse>> vote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long pollId,
            @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                pollService.vote(principal.getId(), pollId, request.getOptionId())));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<PollResponse>>> getMyPolls(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                pollService.getMyPolls(principal.getId())));
    }

    @GetMapping("/{pollId}/results")
    public ResponseEntity<ApiResponse<PollResultResponse>> getPollResults(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long pollId) {
        return ResponseEntity.ok(ApiResponse.success(
                pollService.getPollResults(principal.getId(), pollId)));
    }
}
