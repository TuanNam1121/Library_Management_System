package com.library.management.services;

import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.entities.Book;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BookService {
    public List<BookReturnDTO> getAll();
    public List<BookReturnDTO> findBook(BookSearchDTO bookSearchDTO);
}
