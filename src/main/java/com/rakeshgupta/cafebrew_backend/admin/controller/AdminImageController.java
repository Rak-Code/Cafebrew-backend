package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/images")
public class AdminImageController {

    private final ImageStorageService imageStorageService;

    @Autowired
    public AdminImageController(@Lazy ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /**
     * POST /api/admin/images/upload
     * Upload a single image for menu items
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "menu-items") String folder) {
        
        String imageUrl = imageStorageService.uploadImage(file, folder);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    /**
     * POST /api/admin/images/upload-multiple
     * Upload multiple images
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, List<String>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "menu-items") String folder) {
        
        List<String> imageUrls = imageStorageService.uploadImages(files, folder);
        return ResponseEntity.ok(Map.of("imageUrls", imageUrls));
    }

    /**
     * DELETE /api/admin/images
     * Delete an image by URL
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Boolean>> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        boolean deleted = imageStorageService.deleteImage(imageUrl);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }
}
