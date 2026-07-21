package com.library.management.services;

import com.library.management.entities.Category;
import com.library.management.exception.CategoryAlreadyExistsException;
import com.library.management.exception.CategoryHasBorrowedBooksException;
import com.library.management.exception.CategoryNotFoundException;
import com.library.management.repositories.BookRepository;
import com.library.management.repositories.BorrowDetailRepository;
import com.library.management.repositories.CategoryRepository;
import com.library.management.services.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowDetailRepository borrowDetailRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void throwsCategoryNotFoundExceptionWhenCategoryDoesNotExist() {
        when(categoryRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void throwsCategoryAlreadyExistsExceptionWhenCreatingDuplicateName() {
        Category category = new Category();
        category.setName("Văn học");
        when(categoryRepository.existsByName("Văn học")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(category))
                .isInstanceOf(CategoryAlreadyExistsException.class)
                .hasMessageContaining("Văn học");
    }

    @Test
    void throwsCategoryHasBorrowedBooksExceptionWhenDeletingCategoryWithActiveBorrow() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Văn học");
        when(categoryRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(category));
        when(borrowDetailRepository.existsByBookCategoryIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryHasBorrowedBooksException.class)
                .hasMessageContaining("đang được cho mượn");
    }
}
