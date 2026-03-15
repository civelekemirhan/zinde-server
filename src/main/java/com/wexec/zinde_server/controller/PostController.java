package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.request.AddCommentRequest;
import com.wexec.zinde_server.dto.request.CreatePollPostRequest;
import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.CommentResponse;
import com.wexec.zinde_server.dto.response.PageResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Metin / Fotoğraf / Metin+Fotoğraf gönderisi oluşturur.
     * Tip, gönderilen içerikten otomatik belirlenir:
     *   sadece caption → TEXT
     *   sadece image   → PHOTO
     *   her ikisi      → TEXT_PHOTO
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createContentPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "caption", required = false) String caption) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.createContentPost(principal.getId(), image, caption)));
    }

    /**
     * Anket gönderisi oluşturur.
     *
     * Body (JSON):
     *   caption    → isteğe bağlı açıklama metni
     *   title      → anket sorusu/başlığı (zorunlu)
     *   options    → ["Seçenek 1", "Seçenek 2", ...] (min 2, max 10)
     *   expiresAt  → "2025-12-31T23:59:59" (zorunlu, gelecekte olmalı)
     */
    @PostMapping("/poll")
    public ResponseEntity<ApiResponse<PostResponse>> createPollPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePollPostRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.createPollPost(principal.getId(), request)));
    }

    /**
     * Post güncelleme (sadece sahip).
     *
     * caption    → yeni metin (boş gönderilirse temizlenir)
     * image      → yeni fotoğraf (sadece PHOTO ve TEXT_PHOTO için geçerli)
     *
     * PostType güncelleme sonrasındaki içeriğe göre otomatik hesaplanır.
     * POLL gönderilerinde yalnızca caption güncellenebilir.
     */
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "caption", required = false) String caption) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.updatePost(principal.getId(), postId, image, caption)));
    }

    /**
     * Post silme (sadece sahip).
     * Fotoğraf, anket, oy, yorum ve beğeniler de silinir.
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId) {
        postService.deletePost(principal.getId(), postId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getFeed(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                new PageResponse<>(postService.getFeed(principal.getId(), page, size))));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostResponse>> likePost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.likePost(principal.getId(), postId)));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostResponse>> unlikePost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.unlikePost(principal.getId(), postId)));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody AddCommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.addComment(principal.getId(), postId, request.getContent(), request.getParentId())));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.getComments(postId)));
    }
}
