package com.rakeshgupta.cafebrew_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@Lazy
public class R2Config {

    @Value("${r2.account.id}")
    private String accountId;

    @Value("${r2.access.key}")
    private String accessKey;

    @Value("${r2.secret.key}")
    private String secretKey;

    @Bean
    @Lazy
    public S3Client r2Client() {
        String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);
        
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
    }
}
