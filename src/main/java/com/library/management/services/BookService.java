package com.library.management.services;

import com.library.management.dto.BookFormDTO;
import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.entities.Book;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {
    List<BookReturnDTO> getAll();

    Page<BookReturnDTO> searchBooks(BookSearchDTO searchDTO);

    BookReturnDTO getBookById(Long id);

    BookReturnDTO getBookByIdForAdmin(Long id);

    BookReturnDTO createBook(BookFormDTO formDTO);

    BookReturnDTO updateBook(Long id, BookFormDTO formDTO);

    void deleteBook(Long id);

    Book findByAuthor(long id);
}
