package com.library.management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookReturnDTO {
    private String bookName;
    private String categories;
    private String authors;
    private int availableQuantity;
}
