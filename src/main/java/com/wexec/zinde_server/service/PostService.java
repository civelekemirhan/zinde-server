package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.CommentResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ModerationService moderationService;

    @Transactional
    public PostResponse createPost(UUID userId, MultipartFile image, String caption) {
        User user = getUser(userId);

        if (image == null || image.isEmpty()) {
            throw new AppException("IMAGE_REQUIRED", "Fotoğraf zorunludur.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel dosyaları yüklenebilir.");
        }

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }

        String imageKey = storageService.uploadFile(imageBytes, "posts", contentType);

        Post post = Post.builder()
                .user(user)
                .imageKey(imageKey)
                .caption(caption)
                .moderationStatus(ModerationStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .build();

        postRepository.save(post);
        moderationService.moderate(post, imageBytes);

        return toPostResponse(post, userId);
    }

    public Page<PostResponse> getFeed(UUID userId, int page, int size) {
        return postRepository
                .findByModerationStatusOrderByCreatedAtDesc(ModerationStatus.APPROVED, PageRequest.of(page, size))
                .map(p -> toPostResponse(p, userId));
    }

    @Transactional
    public PostResponse likePost(UUID userId, Long postId) {
        User user = getUser(userId);
        Post post = getPost(postId);

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new AppException("ALREADY_LIKED", "Bu gönderiyi zaten beğendiniz.");
        }

        postLikeRepository.save(PostLike.builder().post(post).user(user).build());
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        return toPostResponse(post, userId);
    }

    @Transactional
    public PostResponse unlikePost(UUID userId, Long postId) {
        User user = getUser(userId);
        Post post = getPost(postId);

        PostLike like = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new AppException("NOT_LIKED", "Bu gönderiyi beğenmediniz."));

        postLikeRepository.delete(like);
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);

        return toPostResponse(post, userId);
    }

    @Transactional
    public CommentResponse addComment(UUID userId, Long postId, String content) {
        User user = getUser(userId);
        Post post = getPost(postId);

        PostComment comment = PostComment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();

        postCommentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return toCommentResponse(comment);
    }

    public Page<CommentResponse> getComments(Long postId, int page, int size) {
        getPost(postId);
        return postCommentRepository
                .findByPostIdOrderByCreatedAtAsc(postId, PageRequest.of(page, size))
                .map(this::toCommentResponse);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Gönderi bulunamadı.", HttpStatus.NOT_FOUND));
    }

    private PostResponse toPostResponse(Post post, UUID currentUserId) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .imageUrl(storageService.getPublicUrl(post.getImageKey()))
                .caption(post.getCaption())
                .moderationStatus(post.getModerationStatus())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByMe(postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId))
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommentResponse toCommentResponse(PostComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
