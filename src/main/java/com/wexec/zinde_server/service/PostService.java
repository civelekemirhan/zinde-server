package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.CreatePollPostRequest;
import com.wexec.zinde_server.dto.response.CommentResponse;
import com.wexec.zinde_server.dto.response.PollResponse;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final FollowRequestRepository followRequestRepository;
    private final StorageService storageService;
    private final ModerationService moderationService;
    private final PollService pollService;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final PollOptionRepository pollOptionRepository;
    private final MentionService mentionService;
    private final MentionRepository mentionRepository;

    /**
     * TEXT / PHOTO / TEXT_PHOTO gönderisi oluşturur.
     * Tip, gönderilen içerikten otomatik belirlenir:
     *   sadece caption → TEXT
     *   sadece image   → PHOTO
     *   her ikisi      → TEXT_PHOTO
     */
    @Transactional
    public PostResponse createContentPost(UUID userId, MultipartFile image, String caption) {
        User user = getUser(userId);

        boolean hasCaption = caption != null && !caption.isBlank();
        boolean hasImage = image != null && !image.isEmpty();

        if (!hasCaption && !hasImage) {
            throw new AppException("EMPTY_POST", "En az bir metin veya fotoğraf gereklidir.");
        }

        String imageKey = null;
        byte[] imageBytes = null;

        if (hasImage) {
            imageBytes = requireImage(image);
            imageKey = storageService.uploadFile(imageBytes, "posts", image.getContentType());
        }

        PostType type;
        if (hasCaption && hasImage) type = PostType.TEXT_PHOTO;
        else if (hasImage)           type = PostType.PHOTO;
        else                         type = PostType.TEXT;

        Post post = Post.builder()
                .user(user)
                .postType(type)
                .imageKey(imageKey)
                .caption(hasCaption ? caption.trim() : null)
                .moderationStatus(hasImage ? ModerationStatus.PENDING : ModerationStatus.APPROVED)
                .likeCount(0)
                .commentCount(0)
                .build();

        postRepository.save(post);

        if (imageBytes != null) {
            moderationService.moderate(post, imageBytes);
        }

        if (post.getCaption() != null) {
            mentionService.processPostMentions(user, post, post.getCaption());
        }

        return toPostResponse(post, userId);
    }

    /**
     * Anket gönderisi oluşturur.
     */
    @Transactional
    public PostResponse createPollPost(UUID userId, CreatePollPostRequest request) {
        User user = getUser(userId);

        String caption = request.getCaption() != null && !request.getCaption().isBlank()
                ? request.getCaption().trim() : null;

        Post post = Post.builder()
                .user(user)
                .postType(PostType.POLL)
                .caption(caption)
                .moderationStatus(ModerationStatus.APPROVED)
                .likeCount(0)
                .commentCount(0)
                .build();

        postRepository.save(post);
        pollService.createPoll(post, request.getTitle(), request.getOptions(), request.getExpiresAt());

        if (caption != null) {
            mentionService.processPostMentions(user, post, caption);
        }

        return toPostResponse(post, userId);
    }

    @Transactional
    public PostResponse updatePost(UUID userId, Long postId, MultipartFile image, String caption) {
        Post post = getPost(postId);

        if (!post.getUser().getId().equals(userId)) {
            throw new AppException("FORBIDDEN", "Bu gönderiyi düzenleme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        if (post.getPostType() == PostType.POLL) {
            // Ankette sadece caption güncellenebilir
            if (caption != null) {
                post.setCaption(caption.isBlank() ? null : caption.trim());
            }
            postRepository.save(post);
            return toPostResponse(post, userId);
        }

        byte[] imageBytes = null;

        // Yeni fotoğraf varsa yükle, eskisini sil
        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new AppException("INVALID_FILE_TYPE", "Sadece görsel dosyaları yüklenebilir.");
            }
            try {
                imageBytes = image.getBytes();
            } catch (IOException e) {
                throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
            }
            if (post.getImageKey() != null) {
                storageService.deleteFile(post.getImageKey());
            }
            post.setImageKey(storageService.uploadFile(imageBytes, "posts", contentType));
            post.setModerationStatus(ModerationStatus.PENDING);
        }

        if (caption != null) {
            post.setCaption(caption.isBlank() ? null : caption.trim());
        }

        // PostType'ı mevcut duruma göre yeniden hesapla
        boolean hasImage = post.getImageKey() != null;
        boolean hasCaption = post.getCaption() != null && !post.getCaption().isBlank();

        if (hasImage && hasCaption) {
            post.setPostType(PostType.TEXT_PHOTO);
        } else if (hasImage) {
            post.setPostType(PostType.PHOTO);
        } else if (hasCaption) {
            post.setPostType(PostType.TEXT);
            // Fotoğraf kaldırılırsa moderasyon durumunu güncelle
            if (post.getModerationStatus() == ModerationStatus.PENDING) {
                post.setModerationStatus(ModerationStatus.APPROVED);
            }
        } else {
            throw new AppException("EMPTY_POST", "Gönderi içeriği boş olamaz.");
        }

        postRepository.save(post);

        if (imageBytes != null) {
            moderationService.moderate(post, imageBytes);
        }

        return toPostResponse(post, userId);
    }

    @Transactional
    public void deletePost(UUID userId, Long postId) {
        Post post = getPost(postId);

        if (!post.getUser().getId().equals(userId)) {
            throw new AppException("FORBIDDEN", "Bu gönderiyi silme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        // Anket varsa oylar → seçenekler → anket sırasıyla sil
        if (post.getPostType() == PostType.POLL) {
            pollRepository.findByPostId(postId).ifPresent(poll -> {
                pollVoteRepository.deleteByPollId(poll.getId());
                pollOptionRepository.deleteByPollId(poll.getId());
                pollRepository.delete(poll);
            });
        }

        postLikeRepository.deleteByPostId(postId);
        mentionRepository.deleteByPostId(postId);
        postCommentRepository.deleteByPostId(postId);

        if (post.getImageKey() != null) {
            storageService.deleteFile(post.getImageKey());
        }

        postRepository.delete(post);
    }

    /** Genel keşif feed'i — tüm onaylı postlar */
    public Page<PostResponse> getFeed(UUID userId, int page, int size) {
        return postRepository
                .findByModerationStatusOrderByCreatedAtDesc(ModerationStatus.APPROVED, PageRequest.of(page, size))
                .map(p -> toPostResponse(p, userId));
    }

    /** Takip edilen kişilerin postları */
    public Page<PostResponse> getFollowingFeed(UUID userId, int page, int size) {
        List<UUID> followingIds = followRequestRepository.findByFromUserId(userId)
                .stream()
                .map(f -> f.getToUser().getId())
                .collect(java.util.stream.Collectors.toList());

        if (followingIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        return postRepository
                .findFollowingFeed(followingIds, PageRequest.of(page, size))
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
    public CommentResponse addComment(UUID userId, Long postId, String content, Long parentId) {
        User user = getUser(userId);
        Post post = getPost(postId);

        PostComment parent = null;
        if (parentId != null) {
            parent = postCommentRepository.findById(parentId)
                    .orElseThrow(() -> new AppException("COMMENT_NOT_FOUND", "Yorum bulunamadı.", HttpStatus.NOT_FOUND));
            if (!parent.getPost().getId().equals(postId)) {
                throw new AppException("COMMENT_POST_MISMATCH", "Yorum bu gönderiye ait değil.");
            }
            parent.setReplyCount(parent.getReplyCount() + 1);
            postCommentRepository.save(parent);
        }

        PostComment comment = PostComment.builder()
                .post(post)
                .parent(parent)
                .user(user)
                .content(content)
                .replyCount(0)
                .build();

        postCommentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // Yorum içeriğindeki @mention'ları işle
        mentionService.processCommentMentions(user, comment, content);

        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(UUID userId, Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("COMMENT_NOT_FOUND", "Yorum bulunamadı.", HttpStatus.NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException("FORBIDDEN", "Bu yorumu silme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        Post post = comment.getPost();
        int deletedCount = countSubtree(commentId);

        // Üst yorumun replyCount'ını düşür
        if (comment.getParent() != null) {
            PostComment parent = comment.getParent();
            parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
            postCommentRepository.save(parent);
        }

        deleteSubtree(commentId);

        post.setCommentCount(Math.max(0, post.getCommentCount() - deletedCount));
        postRepository.save(post);
    }

    public List<CommentResponse> getComments(Long postId) {
        getPost(postId);
        return postCommentRepository
                .findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId)
                .stream().map(this::toCommentResponse).collect(java.util.stream.Collectors.toList());
    }

    // ── Yardımcı metodlar ────────────────────────────────────────────────────

    private byte[] requireImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new AppException("IMAGE_REQUIRED", "Bu gönderi tipi için fotoğraf zorunludur.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel dosyaları yüklenebilir.");
        }
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }
    }

    // UserService'ten de çağrılabilmesi için package-private
    PostResponse toPublicPostResponse(Post post, UUID currentUserId) {
        return toPostResponse(post, currentUserId);
    }

    private PostResponse toPostResponse(Post post, UUID currentUserId) {
        PostType type = post.getPostType() != null ? post.getPostType() : PostType.PHOTO;
        String imageUrl = post.getImageKey() != null ? storageService.getPublicUrl(post.getImageKey()) : null;
        String avatarUrl = post.getUser().getAvatarKey() != null
                ? storageService.getPublicUrl(post.getUser().getAvatarKey())
                : null;
        PollResponse poll = type == PostType.POLL
                ? pollService.getPollByPostId(post.getId(), currentUserId)
                : null;

        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .avatarUrl(avatarUrl)
                .role(post.getUser().getRole())
                .postType(type)
                .imageUrl(imageUrl)
                .caption(post.getCaption())
                .moderationStatus(post.getModerationStatus())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByMe(postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId))
                .poll(poll)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommentResponse toCommentResponse(PostComment comment) {
        String avatarUrl = comment.getUser().getAvatarKey() != null
                ? storageService.getPublicUrl(comment.getUser().getAvatarKey())
                : null;

        List<CommentResponse> replies = postCommentRepository
                .findByParentId(comment.getId())
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .avatarUrl(avatarUrl)
                .role(comment.getUser().getRole())
                .content(comment.getContent())
                .replies(replies)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // Alt ağaçtaki toplam yorum sayısını hesapla (silmeden önce post.commentCount güncelleme için)
    private int countSubtree(Long commentId) {
        List<PostComment> children = postCommentRepository.findByParentId(commentId);
        int count = 1;
        for (PostComment child : children) {
            count += countSubtree(child.getId());
        }
        return count;
    }

    // Bir yorumu ve tüm alt yanıtlarını sil (derinlik önce)
    private void deleteSubtree(Long commentId) {
        List<PostComment> children = postCommentRepository.findByParentId(commentId);
        for (PostComment child : children) {
            deleteSubtree(child.getId());
        }
        mentionRepository.deleteByCommentId(commentId);
        postCommentRepository.deleteById(commentId);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Gönderi bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
