package com.wexec.zinde_server.controller;

import com.wexec.zinde_server.dto.response.ApiResponse;
import com.wexec.zinde_server.dto.response.CommentResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.security.UserPrincipal;
import com.wexec.zinde_server.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "caption", required = false) String caption) {
        return ResponseEntity.ok(ApiResponse.success(
                postService.createPost(principal.getId(), image, caption)));
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
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CONTENT_REQUIRED", "Yorum içeriği boş olamaz."));
        }
        return ResponseEntity.ok(ApiResponse.success(
                postService.addComment(principal.getId(), postId, content)));
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
