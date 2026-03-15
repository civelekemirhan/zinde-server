package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.response.ChatMessageResponse;
import com.wexec.zinde_server.dto.response.ConversationResponse;
import com.wexec.zinde_server.dto.response.UserSummaryResponse;
import com.wexec.zinde_server.entity.*;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConversationResponse getOrCreateDirect(UUID currentUserId, UUID participantId) {
        if (currentUserId.equals(participantId)) {
            throw new AppException("SELF_CONVERSATION", "Kendinizle konuşma başlatamazsınız.");
        }

        User current = getUser(currentUserId);
        User other = getUser(participantId);

        return conversationRepository
                .findDirectConversation(ConversationType.DIRECT, List.of(currentUserId, participantId))
                .map(c -> toResponse(c, currentUserId))
                .orElseGet(() -> {
                    Conversation conv = conversationRepository.save(
                            Conversation.builder().type(ConversationType.DIRECT).build());
                    participantRepository.save(ConversationParticipant.builder()
                            .conversation(conv).user(current).build());
                    participantRepository.save(ConversationParticipant.builder()
                            .conversation(conv).user(other).build());
                    return toResponse(conv, currentUserId);
                });
    }

    public List<ConversationResponse> getMyConversations(UUID userId) {
        return conversationRepository.findByParticipantUserId(userId).stream()
                .map(c -> toResponse(c, userId))
                .collect(Collectors.toList());
    }

    public Page<ChatMessageResponse> getMessages(UUID conversationId, UUID requesterId, int page, int size) {
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, requesterId)) {
            throw new AppException("NOT_PARTICIPANT", "Bu konuşmaya erişim yetkiniz yok.", HttpStatus.FORBIDDEN);
        }
        return messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(page, size))
                .map(this::toMessageResponse);
    }

    public ConversationResponse toResponse(Conversation conv, UUID currentUserId) {
        List<UserSummaryResponse> participants = participantRepository
                .findByConversationId(conv.getId()).stream()
                .map(cp -> toUserSummary(cp.getUser()))
                .collect(Collectors.toList());

        ChatMessageResponse lastMessage = messageRepository
                .findTopByConversationIdOrderByCreatedAtDesc(conv.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        return ConversationResponse.builder()
                .id(conv.getId())
                .type(conv.getType())
                .title(conv.getTitle())
                .participants(participants)
                .lastMessage(lastMessage)
                .createdAt(conv.getCreatedAt())
                .build();
    }

    public ChatMessageResponse toMessageResponse(com.wexec.zinde_server.entity.Message msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .senderId(msg.getSender().getId())
                .senderUsername(msg.getSender().getUsername())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    private UserSummaryResponse toUserSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
