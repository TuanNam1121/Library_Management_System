package com.library.management.services.impl;

import com.library.management.services.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final String UPLOAD_URL_PREFIX = "/uploads/";
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif"
    );

    private final Path uploadRoot;

    public FileStorageServiceImpl(@Value("${app.upload.dir:uploads}") String uploadDirectory) {
        this.uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String saveFile(MultipartFile file) {
        return saveFile(file, "");
    }

    @Override
    public String saveFile(MultipartFile file, String subdirectory) {
        validateImage(file);

        try {
            Path uploadPath = resolveUploadDirectory(subdirectory);
            Files.createDirectories(uploadPath);

            String contentType = file.getContentType().toLowerCase(Locale.ROOT);
            String extension = ALLOWED_IMAGE_TYPES.get(contentType);
            String newFilename = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(newFilename).normalize();

            if (!targetPath.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Đường dẫn lưu ảnh không hợp lệ.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = uploadRoot.relativize(targetPath)
                    .toString()
                    .replace('\\', '/');
            return UPLOAD_URL_PREFIX + relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh.", e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            String normalizedUrl = filePath.replace('\\', '/');
            String relativePath;

            if (normalizedUrl.startsWith(UPLOAD_URL_PREFIX)) {
                relativePath = normalizedUrl.substring(UPLOAD_URL_PREFIX.length());
            } else if (normalizedUrl.startsWith("uploads/")) {
                relativePath = normalizedUrl.substring("uploads/".length());
            } else {
                return;
            }

            Path targetPath = uploadRoot.resolve(relativePath).normalize();
            if (targetPath.startsWith(uploadRoot)) {
                Files.deleteIfExists(targetPath);
            }
        } catch (IOException e) {
            System.err.println("Không thể xóa file ảnh cũ: " + filePath + " — " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn một file ảnh.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !ALLOWED_IMAGE_TYPES.containsKey(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG, PNG hoặc GIF.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new IllegalArgumentException("File được chọn không phải là ảnh hợp lệ.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc file ảnh.", e);
        }
    }

    private Path resolveUploadDirectory(String subdirectory) {
        if (subdirectory == null || subdirectory.isBlank()) {
            return uploadRoot;
        }

        Path relativeDirectory = Paths.get(subdirectory).normalize();
        if (relativeDirectory.isAbsolute() || relativeDirectory.startsWith("..")) {
            throw new IllegalArgumentException("Thư mục lưu ảnh không hợp lệ.");
        }

        Path resolvedDirectory = uploadRoot.resolve(relativeDirectory).normalize();
        if (!resolvedDirectory.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Thư mục lưu ảnh không hợp lệ.");
        }
        return resolvedDirectory;
    }
}
