package com.swigg.filestorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir:./uploads/restaurant-images}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String entityType) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = entityType + "_" + UUID.randomUUID() + fileExtension;

            // Store file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully: {}", filePath.toAbsolutePath());
            return "/uploads/restaurant-images/" + uniqueFilename;
        } catch (IOException e) {
            logger.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir).resolve(filename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    logger.info("File deleted successfully: {}", filePath.toAbsolutePath());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", fileUrl, e);
        }
    }
}
