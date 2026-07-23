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
        validateBookForm(formDTO, null);

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

        validateBookForm(formDTO, id);

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

        if (book.getQuantity() != null && book.getAvailableQuantity() != null
                && book.getAvailableQuantity() < book.getQuantity()) {
            int borrowedCount = book.getQuantity() - book.getAvailableQuantity();
            throw new IllegalArgumentException("Không thể xóa sách vì đang có " + borrowedCount + " bản được độc giả mượn chưa trả!");
        }

        book.setIsDeleted(true);
        book.setDeletedAt(LocalDateTime.now());
        bookRepository.save(book);
    }

    @Override
    public Book findByAuthor(long id) {
        return bookRepository.existAuthor(id);
    }

    @Override
    public List<Book> findByAuthorID(long id) {
        return bookRepository.findByAuthor(id);
    }

    private void validateBookForm(BookFormDTO formDTO, Long currentBookId) {
        if (formDTO == null) {
            throw new IllegalArgumentException("Dữ liệu sách không được để trống!");
        }

        if (formDTO.getTitle() == null || formDTO.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tên sách không được để trống!");
        }

        if (formDTO.getQuantity() == null || formDTO.getQuantity() < 0) {
            throw new IllegalArgumentException("Tổng số bản phải lớn hơn hoặc bằng 0!");
        }
        if (formDTO.getAvailableQuantity() == null || formDTO.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException("Số bản có thể mượn phải lớn hơn hoặc bằng 0!");
        }
        if (formDTO.getAvailableQuantity() > formDTO.getQuantity()) {
            throw new IllegalArgumentException("Số bản có thể mượn (" + formDTO.getAvailableQuantity() 
                    + ") không được lớn hơn tổng số bản (" + formDTO.getQuantity() + ")!");
        }

        if (formDTO.getIsbn() != null && !formDTO.getIsbn().isBlank()) {
            String trimmedIsbn = formDTO.getIsbn().trim();
            if (currentBookId == null) {
                if (bookRepository.existsByIsbn(trimmedIsbn)) {
                    throw new IllegalArgumentException("Mã ISBN \"" + trimmedIsbn + "\" đã tồn tại trong hệ thống!");
                }
            } else {
                if (bookRepository.existsByIsbnAndIdNot(trimmedIsbn, currentBookId)) {
                    throw new IllegalArgumentException("Mã ISBN \"" + trimmedIsbn + "\" đã thuộc về một cuốn sách khác!");
                }
            }
        }
    }

    private void applyCategory(Book book, Long categoryId) {
        if (categoryId != null && categoryId > 0) {
            Category category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Thể loại được chọn không tồn tại!"));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }

    private void applyAuthor(Book book, Long authorId) {
        if (authorId != null && authorId > 0) {
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new EntityNotFoundException("Tác giả được chọn không tồn tại!"));
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
    }

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

