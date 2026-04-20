package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.CreateConversationRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.ChatMessageResponse;
import com.wexec.zinde_server.dto.response.ConversationResponse;
import com.wexec.zinde_server.entity.MessageType;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.ConversationService;
import com.wexec.zinde_server.service.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessagingService messagingService;

    /** Doğrudan konuşma başlat veya mevcut olanı getir */
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateConversationRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getOrCreateDirect(principal.getId(), req.getParticipantId())));
    }

    /** Kendi konuşma listesini getir */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getMyConversations(principal.getId())));
    }

    /** Konuşmanın mesaj geçmişi (yeniden eskiye) */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getMessages(conversationId, principal.getId(), page, size)));
    }

    /**
     * Medya mesajı gönder (IMAGE / AUDIO / TEXT_IMAGE).
     * Dosyayı yükler, kaydeder ve katılımcılara WebSocket push yapar.
     *
     * Form fields:
     *   file        → zorunlu (görsel veya ses dosyası)
     *   messageType → IMAGE | AUDIO | TEXT_IMAGE
     *   caption     → isteğe bağlı metin (TEXT_IMAGE için)
     */
    @PostMapping(value = "/{conversationId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMedia(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("messageType") String messageTypeStr,
            @RequestPart(value = "caption", required = false) String caption) {

        MessageType messageType;
        try {
            messageType = MessageType.valueOf(messageTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("INVALID_MESSAGE_TYPE", "Geçersiz mesaj tipi. IMAGE, AUDIO veya TEXT_IMAGE olmalı.");
        }

        if (messageType == MessageType.TEXT) {
            throw new AppException("INVALID_MESSAGE_TYPE", "Bu endpoint sadece medya mesajları için kullanılır.");
        }

        return ResponseEntity.ok(ApiResponse.success(
                messagingService.sendMediaMessage(principal.getId(), conversationId, messageType, caption, file)));
    }
}
