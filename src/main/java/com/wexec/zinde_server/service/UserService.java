package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.UpdateProfileRequest;
import com.wexec.zinde_server.dto.response.PackagePurchaseSummaryResponse;
import com.wexec.zinde_server.dto.response.PostResponse;
import com.wexec.zinde_server.dto.response.TrainerProfileResponse;
import com.wexec.zinde_server.dto.response.UserProfileResponse;
import com.wexec.zinde_server.entity.*;

import java.util.List;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.FollowRequestRepository;
import com.wexec.zinde_server.repository.PackagePurchaseRepository;
import com.wexec.zinde_server.repository.PostRepository;
import com.wexec.zinde_server.repository.TrainerApplicationRepository;
import com.wexec.zinde_server.repository.TrainerProfileRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowRequestRepository followRequestRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final TrainerApplicationRepository trainerApplicationRepository;
    private final PackagePurchaseRepository packagePurchaseRepository;
    private final StorageService storageService;
    private final PostService postService;
    private final TrainerPackageService trainerPackageService;

    public UserProfileResponse getMyProfile(UUID userId) {
        User user = getUser(userId);
        return toProfileResponse(user, null);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUser(userId);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBio() != null) user.setBio(request.getBio());

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new AppException("USERNAME_TAKEN", "Bu kullanıcı adı zaten kullanılıyor.");
            }
            user.setUsername(request.getUsername());
        }

        userRepository.save(user);
        return toProfileResponse(user, null);
    }

    public UserProfileResponse getProfile(UUID viewerId, UUID targetId) {
        User target = getUser(targetId);
        return toProfileResponse(target, viewerId);
    }

    @Transactional
    public UserProfileResponse uploadAvatar(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("FILE_REQUIRED", "Fotoğraf zorunludur.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException("INVALID_FILE_TYPE", "Sadece görsel dosyaları yüklenebilir.");
        }

        byte[] original;
        try {
            original = file.getBytes();
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }

        byte[] resized = resizeToSquare(original, 400);

        User user = getUser(userId);

        // Eski avatarı sil
        if (user.getAvatarKey() != null) {
            storageService.deleteFile(user.getAvatarKey());
        }

        String key = storageService.uploadFile(resized, "avatars", "image/jpeg");
        user.setAvatarKey(key);
        userRepository.save(user);

        return toProfileResponse(user, null);
    }

    @Transactional
    public UserProfileResponse removeAvatar(UUID userId) {
        User user = getUser(userId);
        if (user.getAvatarKey() != null) {
            storageService.deleteFile(user.getAvatarKey());
            user.setAvatarKey(null);
            userRepository.save(user);
        }
        return toProfileResponse(user, null);
    }

    public Page<PostResponse> getUserPosts(UUID viewerId, UUID targetId, int page, int size) {
        getUser(targetId);
        return postRepository
                .findByUserIdAndModerationStatusOrderByCreatedAtDesc(
                        targetId, ModerationStatus.APPROVED, PageRequest.of(page, size))
                .map(p -> postService.toPublicPostResponse(p, viewerId));
    }

    // ── Yardımcı metodlar ────────────────────────────────────────────────────

    private UserProfileResponse toProfileResponse(User target, UUID viewerId) {
        String avatarUrl = target.getAvatarKey() != null
                ? storageService.getPublicUrl(target.getAvatarKey())
                : null;

        long postCount = postRepository.countByUserId(target.getId());
        long followerCount = followRequestRepository.countByToUserId(target.getId());
        long followingCount = followRequestRepository.countByFromUserId(target.getId());

        boolean isMe = viewerId == null || viewerId.equals(target.getId());
        boolean isFollowing = !isMe && followRequestRepository.existsByFromUserIdAndToUserId(viewerId, target.getId());

        TrainerProfileResponse trainerProfile = null;
        if (target.getRole() == UserRole.TRAINER) {
            trainerProfile = trainerProfileRepository.findByUserId(target.getId())
                    .map(tp -> {
                        ApplicationStatus certStatus = trainerApplicationRepository
                                .findTopByUserIdOrderByCreatedAtDesc(target.getId())
                                .map(TrainerApplication::getStatus)
                                .orElse(null);
                        return TrainerProfileResponse.builder()
                                .specializations(tp.getSpecializations())
                                .yearsOfExperience(tp.getYearsOfExperience())
                                .city(tp.getCity())
                                .certificateStatus(certStatus)
                                .build();
                    })
                    .orElse(null);
        }

        List<PackagePurchaseSummaryResponse> activePackages = packagePurchaseRepository
                .findByUserIdOrderByPurchasedAtDesc(target.getId())
                .stream()
                .map(trainerPackageService::toPurchaseSummaryPublic)
                .collect(java.util.stream.Collectors.toList());

        return UserProfileResponse.builder()
                .id(target.getId())
                .username(target.getUsername())
                .firstName(target.getFirstName())
                .lastName(target.getLastName())
                .bio(target.getBio())
                .avatarUrl(avatarUrl)
                .gender(target.getGender())
                .role(target.getRole())
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .me(isMe)
                .createdAt(target.getCreatedAt())
                .trainerProfile(trainerProfile)
                .activePackages(activePackages)
                .build();
    }

    private byte[] resizeToSquare(byte[] imageBytes, int size) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(size, size)
                    .crop(Positions.CENTER)
                    .outputFormat("jpeg")
                    .outputQuality(0.85)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException("IMAGE_PROCESS_ERROR", "Fotoğraf işlenemedi.");
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
