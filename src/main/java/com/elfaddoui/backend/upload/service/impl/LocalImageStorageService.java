package com.elfaddoui.backend.upload.service.impl;

import com.elfaddoui.backend.config.AppProperties;
import com.elfaddoui.backend.upload.service.ImageStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalImageStorageService implements ImageStorageService {

    private final AppProperties appProperties;

    public LocalImageStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase(Locale.ROOT) : extensionFromContentType(contentType);
        String filename = UUID.randomUUID() + extension;

        try {
            Path directory = Path.of(appProperties.getUploads().getDirectory()).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store image");
        }

        String baseUrl = appProperties.getUploads().getPublicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "/uploads";
        }
        return baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".img";
        };
    }
}
