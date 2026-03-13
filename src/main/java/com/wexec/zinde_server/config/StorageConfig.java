package com.wexec.zinde_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class StorageConfig {

    @Value("${storage.endpoint}")
    private String storageEndpoint;

    @Value("${storage.access-key}")
    private String storageAccessKey;

    @Value("${storage.secret-key}")
    private String storageSecretKey;

    @Value("${storage.region:auto}")
    private String storageRegion;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.access-key}")
    private String awsAccessKey;

    @Value("${aws.secret-key}")
    private String awsSecretKey;

    // Cloudflare R2 (S3-compatible) client
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(storageEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(storageAccessKey, storageSecretKey)))
                .region(Region.of(storageRegion))
                .build();
    }

    // AWS Rekognition for image moderation
    @Bean
    public RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
    }
}
