package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.PollResponse;
import com.wexec.zinde_server.dto.response.PollResultResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<ApiResponse<PollResponse>> vote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long pollId,
            @RequestBody Map<String, Long> body) {
        Long optionId = body.get("optionId");
        if (optionId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("OPTION_ID_REQUIRED", "Seçenek ID zorunludur."));
        }
        return ResponseEntity.ok(ApiResponse.success(
                pollService.vote(principal.getId(), pollId, optionId)));
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
