package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.CommentResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.PostService;
import lombok.RequiredArgsConstructor;
import com.wexec.zinde_server.dto.request.AddCommentRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
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
     * Post oluşturma.
     *
     * TEXT       → type=TEXT,       caption (zorunlu)
     * PHOTO      → type=PHOTO,      image (zorunlu)
     * TEXT_PHOTO → type=TEXT_PHOTO, image (zorunlu), caption (zorunlu)
     * POLL       → type=POLL,       pollTitle (zorunlu), pollOptions JSON array (zorunlu),
     *                                pollExpiresAt ISO datetime (zorunlu), caption (isteğe bağlı)
     *
     * type verilmezse PHOTO kabul edilir.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart(value = "type", required = false) String type,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "caption", required = false) String caption,
            @RequestPart(value = "pollTitle", required = false) String pollTitle,
            @RequestPart(value = "pollOptions", required = false) String pollOptionsJson,
            @RequestPart(value = "pollExpiresAt", required = false) String pollExpiresAt) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.createPost(principal.getId(), type, image, caption,
                        pollTitle, pollOptionsJson, pollExpiresAt)));
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
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getFeed(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.getFeed(principal.getId(), page, size)));
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
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.getComments(postId, page, size)));
    }
}
