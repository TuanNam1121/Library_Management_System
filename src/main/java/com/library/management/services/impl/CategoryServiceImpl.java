package com.library.management.services.impl;

import com.library.management.entities.Book;
import com.library.management.entities.Category;
import com.library.management.enums.BorrowItemStatus;
import com.library.management.exception.CategoryAlreadyExistsException;
import com.library.management.exception.CategoryHasBorrowedBooksException;
import com.library.management.exception.CategoryNotFoundException;
import com.library.management.repositories.BookRepository;
import com.library.management.repositories.BorrowDetailRepository;
import com.library.management.repositories.CategoryRepository;
import com.library.management.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final BorrowDetailRepository borrowDetailRepository;

    @Override
    public Page<Category> searchCategories(String name, int page) {
        String searchName = name == null ? "" : name.trim();
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(
                safePage,
                10,
                Sort.by("name").ascending()
        );

        return categoryRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(searchName, pageable);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Không tìm thấy thể loại với ID: " + id
                ));
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new CategoryAlreadyExistsException(
                    "Tên thể loại '" + category.getName() + "' đã tồn tại!"
            );
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        if (categoryRepository.existsByNameAndIdNot(category.getName(), id)) {
            throw new CategoryAlreadyExistsException(
                    "Tên thể loại '" + category.getName() + "' đã tồn tại!"
            );
        }
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        List<BorrowItemStatus> activeStatuses = new ArrayList<>();
        activeStatuses.add(BorrowItemStatus.BORROWING);
        activeStatuses.add(BorrowItemStatus.OVERDUE);

        if (borrowDetailRepository.existsByBookCategoryIdAndStatusIn(id, activeStatuses)) {
            throw new CategoryHasBorrowedBooksException(
                    "Không thể xóa thể loại '" + category.getName()
                            + "' vì vẫn còn sách đang được cho mượn."
            );
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        List<Book> books = bookRepository.findAllByCategoryIdAndIsDeletedFalse(id);
        for (Book book : books) {
            book.setIsDeleted(true);
            book.setDeletedAt(deletedAt);
        }

        bookRepository.saveAll(books);
        category.setIsDeleted(true);
        category.setDeletedAt(deletedAt);
        categoryRepository.save(category);
    }
}
