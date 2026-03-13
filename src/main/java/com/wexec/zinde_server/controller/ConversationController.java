package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.CreateConversationRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.ChatMessageResponse;
import com.wexec.zinde_server.dto.response.ConversationResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateConversationRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getOrCreateDirect(principal.getId(), req.getParticipantId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getMyConversations(principal.getId())));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getMessages(conversationId, principal.getId(), page, size)));
    }
}
