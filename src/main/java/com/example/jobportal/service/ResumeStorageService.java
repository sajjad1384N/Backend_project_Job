package com.example.jobportal.service;

import org.springframework.beans.factory.annotation.Value;
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
public class ResumeStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "doc", "docx");

    @Value("${app.resume.upload-dir:uploads/resumes}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public StoredResume store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new IllegalArgumentException("Resume must have a filename");
        }
        String ext = extension(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Resume must be PDF, DOC, or DOCX");
        }
        String key = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalStateException("Invalid storage path");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredResume(key, original);
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

    private static String extension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return "";
        }
        return filename.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredResume(String storageKey, String originalFilename) {}
}
