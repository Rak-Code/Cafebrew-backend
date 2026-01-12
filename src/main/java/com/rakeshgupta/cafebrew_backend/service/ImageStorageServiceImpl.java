package com.rakeshgupta.cafebrew_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Lazy
@RequiredArgsConstructor
@Slf4j
public class ImageStorageServiceImpl implements ImageStorageService {

    private final S3Client r2Client;

    @Value("${r2.bucket.name}")
    private String bucketName;

    @Value("${r2.public.url}")
    private String publicUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename(), folder);
            String contentType = file.getContentType();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            r2Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = publicUrl + "/" + fileName;
            log.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String url = uploadImage(file, folder);
                imageUrls.add(url);
            } catch (Exception e) {
                log.error("Failed to upload image {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }
        return imageUrls;
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            String fileName = extractFileNameFromUrl(imageUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            r2Client.deleteObject(deleteObjectRequest);
            log.info("Image deleted successfully: {}", imageUrl);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete image {}: {}", imageUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteImages(List<String> imageUrls) {
        boolean allDeleted = true;
        for (String imageUrl : imageUrls) {
            if (!deleteImage(imageUrl)) {
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }
    }

    private String generateFileName(String originalFilename, String folder) {
        String extension = getFileExtension(originalFilename);
        String uniqueId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (folder != null && !folder.isEmpty()) {
            return folder + "/" + timestamp + "_" + uniqueId + "." + extension;
        }
        return timestamp + "_" + uniqueId + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String extractFileNameFromUrl(String imageUrl) {
        return imageUrl.replace(publicUrl + "/", "");
    }
}
