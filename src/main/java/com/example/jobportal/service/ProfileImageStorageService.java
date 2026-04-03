package com.example.jobportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileImageStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.profile.upload-dir:uploads/profile-images}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new IllegalArgumentException("Image must have a filename");
        }
        String ext = extension(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Image must be JPG, PNG, GIF, or WEBP");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be at most 2 MB");
        }
        String key = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalStateException("Invalid storage path");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return key;
    }

    public void deleteIfExists(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        Path p = resolveToPath(storageKey);
        if (p != null && Files.isRegularFile(p)) {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    public Path resolveToPath(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }
        if (storageKey.contains("..") || storageKey.contains("/") || storageKey.contains("\\")) {
            return null;
        }
        Path p = root.resolve(storageKey).normalize();
        if (!p.startsWith(root)) {
            return null;
        }
        return p;
    }

    public MediaType mediaTypeForKey(String storageKey) {
        String ext = extension(storageKey);
        return switch (ext) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private static String extension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return "";
        }
        return filename.substring(i + 1).toLowerCase(Locale.ROOT);
    }
}
