package com.library.management.dto;

import com.library.management.enums.BookStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookReturnDTO {
    private Long id;
    private String title;
    private String isbn;
    private String coverImage;
    private String description;
    private Integer quantity;
    private Integer availableQuantity;
    private BookStatus status;
    private String categoryName;
    private String authorName;
    private String authorBio;
}
