package com.kycportal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = Set.of("pdf", "jpg", "jpeg", "png");

    private final Path root;

    public FileStorageService(@Value("${kyc.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Returns an error message, or null when the file is acceptable. */
    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "Choose a document to upload";
        }
        if (!ALLOWED.contains(extensionOf(file.getOriginalFilename()))) {
            return "Upload a PDF, JPG or PNG file";
        }
        return null;
    }

    /** Saves the file under a random name and returns that name. */
    public String store(MultipartFile file) throws IOException {
        Files.createDirectories(root);
        String storedName = UUID.randomUUID() + "." + extensionOf(file.getOriginalFilename());
        Path target = root.resolve(storedName).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    private static String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}