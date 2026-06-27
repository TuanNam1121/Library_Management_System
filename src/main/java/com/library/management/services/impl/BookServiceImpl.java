package com.library.management.services.impl;

import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.entities.Book;
import com.library.management.enums.BookStatus;
import com.library.management.repositories.BookRepository;
import com.library.management.services.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<BookReturnDTO> getAll() {
        return bookRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Page<BookReturnDTO> searchBooks(BookSearchDTO searchDTO) {
        // Trim keyword, treat blank as null
        String keyword = (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isBlank())
                ? searchDTO.getKeyword().trim()
                : null;

        Long authorId = (searchDTO.getAuthorId() != null && searchDTO.getAuthorId() > 0)
                ? searchDTO.getAuthorId()
                : null;

        Long categoryId = (searchDTO.getCategoryId() != null && searchDTO.getCategoryId() > 0)
                ? searchDTO.getCategoryId()
                : null;

        int page = Math.max(searchDTO.getPage(), 0);
        int size = (searchDTO.getSize() > 0) ? searchDTO.getSize() : 9;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return bookRepository.searchBooks(keyword, authorId, categoryId, pageable)
                .map(this::toDTO);
    }

    @Override
    public BookReturnDTO getBookById(Long id) {
        Book book = bookRepository.findByIdAndIsDeletedFalseAndStatus(id, BookStatus.APPROVED)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sách với id: " + id));
        return toDTO(book);
    }

    // ---- Mapping helper ----
    private BookReturnDTO toDTO(Book book) {
        BookReturnDTO dto = new BookReturnDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setCoverImage(book.getCoverImage());
        dto.setDescription(book.getDescription());
        dto.setQuantity(book.getQuantity());
        dto.setAvailableQuantity(book.getAvailableQuantity());
        dto.setStatus(book.getStatus());

        if (book.getCategory() != null) {
            dto.setCategoryName(book.getCategory().getName());
        }
        if (book.getAuthor() != null) {
            dto.setAuthorName(book.getAuthor().getName());
            dto.setAuthorBio(book.getAuthor().getBio());
        }
        return dto;
    }
}
