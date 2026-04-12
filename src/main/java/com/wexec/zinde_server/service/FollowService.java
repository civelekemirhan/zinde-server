package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.UserSummaryResponse;
import com.wexec.zinde_server.entity.FollowRequest;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.FollowRequestRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRequestRepository followRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final StorageService storageService;

    @Transactional
    public void follow(UUID fromId, UUID toId) {
        if (fromId.equals(toId)) {
            throw new AppException("SELF_FOLLOW", "Kendinizi takip edemezsiniz.");
        }
        if (followRepository.existsByFromUserIdAndToUserId(fromId, toId)) {
            throw new AppException("ALREADY_FOLLOWING", "Bu kullanıcıyı zaten takip ediyorsunuz.", HttpStatus.CONFLICT);
        }

        User from = getUser(fromId);
        User to = getUser(toId);

        followRepository.save(FollowRequest.builder()
                .fromUser(from)
                .toUser(to)
                .build());

        if (to.getFcmToken() != null) {
            fcmService.sendToToken(
                    to.getFcmToken(),
                    "Yeni takipçi",
                    from.getUsername() + " seni takip etmeye başladı.",
                    Map.of("type", "NEW_FOLLOWER", "fromUserId", fromId.toString(),
                            "fromUsername", from.getUsername())
            );
        }
    }

    @Transactional
    public void unfollow(UUID fromId, UUID toId) {
        FollowRequest follow = followRepository.findByFromUserIdAndToUserId(fromId, toId)
                .orElseThrow(() -> new AppException("NOT_FOLLOWING", "Bu kullanıcıyı zaten takip etmiyorsunuz.", HttpStatus.NOT_FOUND));
        followRepository.delete(follow);
    }

    public List<UserSummaryResponse> getFollowers(UUID userId) {
        return followRepository.findByToUserId(userId)
                .stream()
                .map(f -> toUserSummary(f.getFromUser()))
                .collect(Collectors.toList());
    }

    public List<UserSummaryResponse> getFollowing(UUID userId) {
        return followRepository.findByFromUserId(userId)
                .stream()
                .map(f -> toUserSummary(f.getToUser()))
                .collect(Collectors.toList());
    }

    private UserSummaryResponse toUserSummary(User user) {
        String avatarUrl = user.getAvatarKey() != null
                ? storageService.getPublicUrl(user.getAvatarKey())
                : null;
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(avatarUrl)
                .build();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
