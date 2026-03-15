package com.wexec.zinde_server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    // Seçenek 1: Railway/prod ortamında JSON içeriğini doğrudan env variable olarak ver
    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    // Seçenek 2: Lokal geliştirmede JSON dosyasının yolunu ver
    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) return;

        try {
            InputStream stream = resolveCredentialStream();
            if (stream == null) {
                log.warn("Firebase yapılandırması bulunamadı. FCM devre dışı. " +
                        "(FIREBASE_SERVICE_ACCOUNT_JSON veya FIREBASE_SERVICE_ACCOUNT_PATH tanımlayın)");
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase başarıyla başlatıldı.");
        } catch (IOException e) {
            log.error("Firebase başlatılamadı: {}", e.getMessage());
        }
    }

    private InputStream resolveCredentialStream() throws IOException {
        // Önce JSON string'i dene (Railway env variable)
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        // Sonra dosya yolunu dene (lokal geliştirme)
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            return new FileInputStream(serviceAccountPath);
        }
        return null;
    }
}
