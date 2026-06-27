package com.library.management.services.impl;

import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.services.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Override
    public List<BookReturnDTO> getAll() {
        return List.of();
    }

    @Override
    public List<BookReturnDTO> findBook(BookSearchDTO bookSearchDTO) {
        return List.of();
    }
}
