package com.wexec.zinde_server.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FcmService {

    private boolean isAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }

    public void sendToToken(String fcmToken, String title, String body) {
        sendToToken(fcmToken, title, body, null);
    }

    public void sendToToken(String fcmToken, String title, String body, Map<String, String> data) {
        if (!isAvailable() || fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM kullanılamıyor veya token boş, bildirim atlandı: {}", title);
            return;
        }
        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
        if (data != null) {
            builder.putAllData(data);
        }
        try {
            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.debug("FCM bildirimi gönderildi: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM bildirimi gönderilemedi (token: {}): {}", fcmToken, e.getMessage());
        }
    }

    public void sendToMultiple(List<String> fcmTokens, String title, String body) {
        sendToMultiple(fcmTokens, title, body, null);
    }

    public void sendToMultiple(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        if (!isAvailable() || fcmTokens == null || fcmTokens.isEmpty()) return;
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
        if (data != null) {
            builder.putAllData(data);
        }
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
            log.debug("FCM toplu bildirim: {} başarılı, {} başarısız",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM toplu bildirim gönderilemedi: {}", e.getMessage());
        }
    }

    public void sendToTopic(String topic, String title, String body) {
        if (!isAvailable()) return;
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM topic bildirimi gönderildi (topic: {}): {}", topic, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM topic bildirimi gönderilemedi: {}", e.getMessage());
        }
    }
}
