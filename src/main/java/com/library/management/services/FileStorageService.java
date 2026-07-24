package com.library.management.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String saveFile(MultipartFile file);

    String saveFile(MultipartFile file, String subdirectory);

    void deleteFile(String filePath);
}
