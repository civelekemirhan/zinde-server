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

    // Prod/Railway: JSON içeriğini env variable olarak ver
    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    // Lokal geliştirme: JSON dosyasının yolunu ver
    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${firebase.storage-bucket}")
    private String storageBucket;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) return;

        try {
            InputStream stream = resolveCredentialStream();
            if (stream == null) {
                log.warn("Firebase yapılandırması bulunamadı. " +
                        "(FIREBASE_SERVICE_ACCOUNT_JSON veya FIREBASE_SERVICE_ACCOUNT_PATH tanımlayın)");
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .setStorageBucket(storageBucket)
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase başarıyla başlatıldı. Storage bucket: {}", storageBucket);
        } catch (IOException e) {
            log.error("Firebase başlatılamadı: {}", e.getMessage());
        }
    }

    private InputStream resolveCredentialStream() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            return new FileInputStream(serviceAccountPath);
        }
        return null;
    }
}
