package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.FollowRequestResponse;
import com.wexec.zinde_server.dto.response.UserSummaryResponse;
import com.wexec.zinde_server.entity.FollowRequest;
import com.wexec.zinde_server.entity.FollowStatus;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.FollowRequestRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRequestRepository followRequestRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final StorageService storageService;

    @Transactional
    public FollowRequestResponse sendRequest(UUID fromId, UUID toId) {
        if (fromId.equals(toId)) {
            throw new AppException("SELF_FOLLOW", "Kendinizi takip edemezsiniz.");
        }
        if (followRequestRepository.existsByFromUserIdAndToUserId(fromId, toId)) {
            throw new AppException("ALREADY_REQUESTED", "Bu kullanıcıya zaten istek gönderildi.");
        }

        User from = getUser(fromId);
        User to = getUser(toId);

        FollowRequest request = followRequestRepository.save(FollowRequest.builder()
                .fromUser(from)
                .toUser(to)
                .status(FollowStatus.PENDING)
                .build());

        // Alıcıya bildirim gönder
        if (to.getFcmToken() != null) {
            fcmService.sendToToken(
                    to.getFcmToken(),
                    "Yeni arkadaşlık isteği",
                    from.getUsername() + " sana arkadaşlık isteği gönderdi.",
                    java.util.Map.of("type", "FOLLOW_REQUEST", "fromUserId", fromId.toString(),
                            "fromUsername", from.getUsername())
            );
        }

        return toResponse(request);
    }

    @Transactional
    public FollowRequestResponse respond(UUID currentUserId, Long requestId, boolean accept) {
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException("REQUEST_NOT_FOUND", "Takip isteği bulunamadı.", HttpStatus.NOT_FOUND));

        if (!request.getToUser().getId().equals(currentUserId)) {
            throw new AppException("FORBIDDEN", "Bu isteğe yanıt verme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }
        if (request.getStatus() != FollowStatus.PENDING) {
            throw new AppException("ALREADY_RESPONDED", "Bu isteğe zaten yanıt verildi.");
        }

        request.setStatus(accept ? FollowStatus.ACCEPTED : FollowStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        followRequestRepository.save(request);

        // Kabul edilirse isteği gönderene bildirim
        if (accept) {
            User sender = request.getFromUser();
            User acceptor = request.getToUser();
            if (sender.getFcmToken() != null) {
                fcmService.sendToToken(
                        sender.getFcmToken(),
                        "Arkadaşlık isteği kabul edildi",
                        acceptor.getUsername() + " arkadaşlık isteğini kabul etti.",
                        java.util.Map.of("type", "FOLLOW_ACCEPTED", "userId", acceptor.getId().toString(),
                                "username", acceptor.getUsername())
                );
            }
        }

        return toResponse(request);
    }

    public List<FollowRequestResponse> getIncomingRequests(UUID userId) {
        return followRequestRepository.findByToUserIdAndStatus(userId, FollowStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UserSummaryResponse> getFriends(UUID userId) {
        return followRequestRepository.findFriends(userId).stream()
                .map(fr -> {
                    User friend = fr.getFromUser().getId().equals(userId) ? fr.getToUser() : fr.getFromUser();
                    return toUserSummary(friend);
                })
                .collect(Collectors.toList());
    }

    private FollowRequestResponse toResponse(FollowRequest fr) {
        return FollowRequestResponse.builder()
                .id(fr.getId())
                .fromUserId(fr.getFromUser().getId())
                .fromUsername(fr.getFromUser().getUsername())
                .toUserId(fr.getToUser().getId())
                .toUsername(fr.getToUser().getUsername())
                .status(fr.getStatus())
                .createdAt(fr.getCreatedAt())
                .build();
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
