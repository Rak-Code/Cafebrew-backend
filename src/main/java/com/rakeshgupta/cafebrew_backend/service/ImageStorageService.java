package com.rakeshgupta.cafebrew_backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageStorageService {
    
    /**
     * Upload a single image to R2 storage.
     * @param file the image file to upload
     * @param folder the folder path in the bucket (e.g., "menu-items")
     * @return the public URL of the uploaded image
     */
    String uploadImage(MultipartFile file, String folder);
    
    /**
     * Upload multiple images to R2 storage.
     * @param files list of image files to upload
     * @param folder the folder path in the bucket
     * @return list of public URLs of the uploaded images
     */
    List<String> uploadImages(List<MultipartFile> files, String folder);
    
    /**
     * Delete an image from R2 storage.
     * @param imageUrl the public URL of the image to delete
     * @return true if deletion was successful
     */
    boolean deleteImage(String imageUrl);
    
    /**
     * Delete multiple images from R2 storage.
     * @param imageUrls list of public URLs to delete
     * @return true if all deletions were successful
     */
    boolean deleteImages(List<String> imageUrls);
}
