package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.SendMessageRequest;
import com.wexec.zinde_server.dto.response.ChatMessageResponse;
import com.wexec.zinde_server.entity.Message;
import com.wexec.zinde_server.entity.User;
import com.wexec.zinde_server.exception.AppException;
import com.wexec.zinde_server.repository.ConversationParticipantRepository;
import com.wexec.zinde_server.repository.ConversationRepository;
import com.wexec.zinde_server.repository.MessageRepository;
import com.wexec.zinde_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;

    @Transactional
    public ChatMessageResponse sendMessage(UUID senderId, SendMessageRequest req) {
        var conversation = conversationRepository.findById(req.getConversationId())
                .orElseThrow(() -> new AppException("CONVERSATION_NOT_FOUND", "Konuşma bulunamadı.", HttpStatus.NOT_FOUND));

        if (!participantRepository.existsByConversationIdAndUserId(conversation.getId(), senderId)) {
            throw new AppException("NOT_PARTICIPANT", "Bu konuşmaya mesaj gönderme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));

        Message message = messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(req.getContent())
                .build());

        ChatMessageResponse response = conversationService.toMessageResponse(message);

        // Konuşmanın tüm katılımcılarına push
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversation.getId(), response);

        return response;
    }
}
