package com.library.management.services.impl;

import com.library.management.dto.BookFormDTO;
import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.entities.Author;
import com.library.management.entities.Book;
import com.library.management.entities.Category;
import com.library.management.enums.BookStatus;
import com.library.management.repositories.AuthorRepository;
import com.library.management.repositories.BookRepository;
import com.library.management.repositories.CategoryRepository;
import com.library.management.services.BookService;
import com.library.management.services.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<BookReturnDTO> getAll() {
        return bookRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Page<BookReturnDTO> searchBooks(BookSearchDTO searchDTO) {
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

    @Override
    public BookReturnDTO getBookByIdForAdmin(Long id) {
        Book book = bookRepository.findById(id)
                .filter(b -> Boolean.FALSE.equals(b.getIsDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sách với id: " + id));
        return toDTO(book);
    }

    @Override
    public BookReturnDTO createBook(BookFormDTO formDTO) {
        Book book = new Book();
        book.setTitle(formDTO.getTitle());
        book.setIsbn(formDTO.getIsbn());
        book.setDescription(formDTO.getDescription());
        book.setQuantity(formDTO.getQuantity());
        book.setAvailableQuantity(formDTO.getAvailableQuantity());
        book.setIsDeleted(false);
        book.setStatus(BookStatus.APPROVED);

        applyCategory(book, formDTO.getCategoryId());
        applyAuthor(book, formDTO.getAuthorId());
        applyCoverImage(book, formDTO.getCoverImageFile(), null);

        return toDTO(bookRepository.save(book));
    }

    @Override
    public BookReturnDTO updateBook(Long id, BookFormDTO formDTO) {
        Book book = bookRepository.findById(id)
                .filter(b -> Boolean.FALSE.equals(b.getIsDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sách với id: " + id));

        book.setTitle(formDTO.getTitle());
        book.setIsbn(formDTO.getIsbn());
        book.setDescription(formDTO.getDescription());
        book.setQuantity(formDTO.getQuantity());
        book.setAvailableQuantity(formDTO.getAvailableQuantity());

        applyCategory(book, formDTO.getCategoryId());
        applyAuthor(book, formDTO.getAuthorId());
        applyCoverImage(book, formDTO.getCoverImageFile(), book.getCoverImage());

        return toDTO(bookRepository.save(book));
    }

    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .filter(b -> Boolean.FALSE.equals(b.getIsDeleted()))
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sách với id: " + id));

        book.setIsDeleted(true);
        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    @Override
    public Book findByAuthor(long id) {
        return bookRepository.existAuthor(id);
    }

    // ---- Helpers ----

    private void applyCategory(Book book, Long categoryId) {
        if (categoryId != null && categoryId > 0) {
            Category category = categoryRepository.findByIdAndIsDeletedFalse(categoryId).orElse(null);
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }

    private void applyAuthor(Book book, Long authorId) {
        if (authorId != null && authorId > 0) {
            Author author = authorRepository.findById(authorId).orElse(null);
            book.setAuthor(author);
        } else {
            book.setAuthor(null);
        }
    }

    private void applyCoverImage(Book book, MultipartFile file, String existingImagePath) {
        if (file != null && !file.isEmpty()) {
            if (existingImagePath != null && !existingImagePath.isBlank()) {
                fileStorageService.deleteFile(existingImagePath);
            }
            book.setCoverImage(fileStorageService.saveFile(file));
        }
        // If no new file provided, keep existing image unchanged
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
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        if (book.getAuthor() != null) {
            dto.setAuthorId(book.getAuthor().getId());
            dto.setAuthorName(book.getAuthor().getName());
            dto.setAuthorBio(book.getAuthor().getBio());
        }
        return dto;
    }
}

