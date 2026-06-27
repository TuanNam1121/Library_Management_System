package com.library.management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookSearchDTO {
    private String keyword;
    private Long authorId;
    private Long categoryId;
    private int page = 0;
    private int size = 9;
}
