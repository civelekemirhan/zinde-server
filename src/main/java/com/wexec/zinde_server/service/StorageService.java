package com.wexec.zinde_server.service;

import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import com.wexec.zinde_server.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class StorageService {

    @Value("${firebase.storage-bucket}")
    private String bucketName;

    public String uploadFile(byte[] data, String folder, String contentType) {
        String objectName = folder + "/" + UUID.randomUUID();
        try {
            StorageClient.getInstance().bucket().create(objectName, data, contentType);
            return objectName;
        } catch (Exception e) {
            log.error("Dosya yüklenemedi ({}): {}", objectName, e.getMessage());
            throw new AppException("UPLOAD_FAILED", "Dosya yüklenemedi.");
        }
    }

    public void deleteFile(String objectName) {
        try {
            Blob blob = StorageClient.getInstance().bucket().get(objectName);
            if (blob != null) {
                blob.delete();
            }
        } catch (Exception e) {
            log.warn("Dosya silinemedi ({}): {}", objectName, e.getMessage());
        }
    }

    public String getPublicUrl(String objectName) {
        String encoded = URLEncoder.encode(objectName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "https://firebasestorage.googleapis.com/v0/b/"
                + bucketName + "/o/" + encoded + "?alt=media";
    }
}
