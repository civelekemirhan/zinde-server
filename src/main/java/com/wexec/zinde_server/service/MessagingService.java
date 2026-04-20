package com.wexec.zinde_server.service;

import com.wexec.zinde_server.dto.request.SendMessageRequest;
import com.wexec.zinde_server.dto.response.ChatMessageResponse;
import com.wexec.zinde_server.entity.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;

    /**
     * WebSocket üzerinden metin (veya mediaKey içeren) mesaj gönderir.
     * Mesaj sadece konuşmanın katılımcılarına iletilir.
     */
    @Transactional
    public ChatMessageResponse sendMessage(UUID senderId, SendMessageRequest req) {
        Conversation conversation = conversationRepository.findById(req.getConversationId())
                .orElseThrow(() -> new AppException("CONVERSATION_NOT_FOUND", "Konuşma bulunamadı.", HttpStatus.NOT_FOUND));

        if (!participantRepository.existsByConversationIdAndUserId(conversation.getId(), senderId)) {
            throw new AppException("NOT_PARTICIPANT", "Bu konuşmaya mesaj gönderme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        validateContent(req);

        User sender = getUser(senderId);

        Message message = messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(sender)
                .messageType(req.getMessageType())
                .content(req.getContent())
                .mediaKey(req.getMediaKey())
                .build());

        ChatMessageResponse response = conversationService.toMessageResponse(message);
        pushToParticipants(conversation.getId(), response);
        return response;
    }

    /**
     * REST üzerinden medya dosyası yükler ve mesaj olarak kaydeder.
     * IMAGE, AUDIO veya TEXT_IMAGE tiplerinde kullanılır.
     * Yükleme tamamlandığında katılımcılara WebSocket ile push yapılır.
     */
    @Transactional
    public ChatMessageResponse sendMediaMessage(UUID senderId, UUID conversationId,
                                                MessageType messageType, String caption,
                                                MultipartFile file) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException("CONVERSATION_NOT_FOUND", "Konuşma bulunamadı.", HttpStatus.NOT_FOUND));

        if (!participantRepository.existsByConversationIdAndUserId(conversationId, senderId)) {
            throw new AppException("NOT_PARTICIPANT", "Bu konuşmaya mesaj gönderme yetkiniz yok.", HttpStatus.FORBIDDEN);
        }

        if (file == null || file.isEmpty()) {
            throw new AppException("FILE_REQUIRED", "Medya dosyası zorunludur.");
        }

        String folder = messageType == MessageType.AUDIO ? "chat-audio" : "chat-images";
        String mediaKey = uploadFile(file, folder);

        String content = (messageType == MessageType.TEXT_IMAGE && caption != null && !caption.isBlank())
                ? caption.trim() : null;

        User sender = getUser(senderId);

        Message message = messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(sender)
                .messageType(messageType)
                .content(content)
                .mediaKey(mediaKey)
                .build());

        ChatMessageResponse response = conversationService.toMessageResponse(message);
        pushToParticipants(conversation.getId(), response);
        return response;
    }

    /**
     * Mesajı yalnızca konuşmanın katılımcılarına gönderir.
     * Her katılımcının kişisel kuyruğuna yazılır: /user/{email}/queue/messages
     * Böylece sadece o kullanıcı alır, başkası dinleyemez.
     */
    private void pushToParticipants(UUID conversationId, ChatMessageResponse response) {
        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversationId);
        for (ConversationParticipant p : participants) {
            messagingTemplate.convertAndSendToUser(
                    p.getUser().getEmail(),
                    "/queue/messages",
                    response
            );
        }
    }

    private void validateContent(SendMessageRequest req) {
        boolean hasText = req.getContent() != null && !req.getContent().isBlank();
        boolean hasMedia = req.getMediaKey() != null && !req.getMediaKey().isBlank();

        switch (req.getMessageType()) {
            case TEXT -> {
                if (!hasText) throw new AppException("CONTENT_REQUIRED", "TEXT mesajı için content zorunludur.");
            }
            case IMAGE, AUDIO -> {
                if (!hasMedia) throw new AppException("MEDIA_REQUIRED", "Medya mesajı için mediaKey zorunludur.");
            }
            case TEXT_IMAGE -> {
                if (!hasText && !hasMedia) throw new AppException("CONTENT_REQUIRED", "TEXT_IMAGE için content veya mediaKey gereklidir.");
            }
        }
    }

    private String uploadFile(MultipartFile file, String folder) {
        try {
            return storageService.uploadFile(file.getBytes(), folder, file.getContentType());
        } catch (IOException e) {
            throw new AppException("FILE_READ_ERROR", "Dosya okunamadı.");
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Kullanıcı bulunamadı.", HttpStatus.NOT_FOUND));
    }
}
