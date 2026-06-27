package com.library.management.services;

import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {
    List<BookReturnDTO> getAll();

    Page<BookReturnDTO> searchBooks(BookSearchDTO searchDTO);

    BookReturnDTO getBookById(Long id);
}
