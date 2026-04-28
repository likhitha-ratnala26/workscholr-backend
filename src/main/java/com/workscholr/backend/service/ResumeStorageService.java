package com.workscholr.backend.service;

import com.workscholr.backend.exception.BadRequestException;
import com.workscholr.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class ResumeStorageService {

    private static final long MAX_RESUME_SIZE_BYTES = 5L * 1024 * 1024;

    private final Path storageRoot;

    public ResumeStorageService(@Value("${app.upload.resume-dir:uploads/resumes}") String resumeDirectory) {
        this.storageRoot = Paths.get(resumeDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize resume storage", exception);
        }
    }

    public StoredResume store(MultipartFile resumeFile) {
        validateResume(resumeFile);

        String originalFileName = sanitizeOriginalFileName(resumeFile.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + ".pdf";
        Path targetPath = resolve(storedFileName);

        try (InputStream inputStream = resumeFile.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store resume PDF", exception);
        }

        return new StoredResume(
                originalFileName,
                storedFileName,
                MediaType.APPLICATION_PDF_VALUE,
                resumeFile.getSize()
        );
    }

    public ResumeDownload load(String storedFileName, String originalFileName, String contentType) {
        Path filePath = resolve(storedFileName);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Resume file not found");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("Resume file not found");
            }

            return new ResumeDownload(
                    resource,
                    originalFileName,
                    StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_PDF_VALUE
            );
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Failed to read resume file", exception);
        }
    }

    public void deleteQuietly(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            return;
        }

        try {
            Files.deleteIfExists(resolve(storedFileName));
        } catch (IOException ignored) {
        }
    }

    private void validateResume(MultipartFile resumeFile) {
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new BadRequestException("Resume PDF is required.");
        }

        if (resumeFile.getSize() > MAX_RESUME_SIZE_BYTES) {
            throw new BadRequestException("Resume PDF must be 5 MB or smaller.");
        }

        String originalFileName = sanitizeOriginalFileName(resumeFile.getOriginalFilename());
        String contentType = resumeFile.getContentType();
        boolean hasPdfExtension = originalFileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean hasPdfContentType = !StringUtils.hasText(contentType)
                || MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)
                || "application/x-pdf".equalsIgnoreCase(contentType);

        if (!hasPdfExtension || !hasPdfContentType) {
            throw new BadRequestException("Only PDF resume files are allowed.");
        }
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        String cleaned = StringUtils.cleanPath(originalFileName == null ? "resume.pdf" : originalFileName);
        return StringUtils.hasText(cleaned) ? cleaned : "resume.pdf";
    }

    private Path resolve(String storedFileName) {
        Path resolved = storageRoot.resolve(storedFileName).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new BadRequestException("Invalid resume file path.");
        }
        return resolved;
    }

    public record StoredResume(
            String originalFileName,
            String storedFileName,
            String contentType,
            long sizeBytes
    ) {
    }

    public record ResumeDownload(
            Resource resource,
            String originalFileName,
            String contentType
    ) {
    }
}
