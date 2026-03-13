package com.wexec.zinde_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${storage.bucket}")
    private String bucket;

    @Value("${storage.public-url}")
    private String publicUrl;

    public String uploadFile(byte[] data, String folder, String contentType) {
        String key = folder + "/" + UUID.randomUUID();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        return key;
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public String getPublicUrl(String key) {
        return publicUrl + "/" + key;
    }
}
