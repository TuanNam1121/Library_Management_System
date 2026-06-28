package com.library.management.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class BookFormDTO {
    private String title;
    private String isbn;
    private String description;
    private Integer quantity;
    private Integer availableQuantity;
    private Long categoryId;
    private Long authorId;
    private MultipartFile coverImageFile;
}
