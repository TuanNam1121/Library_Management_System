package com.library.management.services.impl;

import com.library.management.entities.Category;
import com.library.management.repositories.CategoryRepository;
import com.library.management.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Tên thể loại '" + category.getName() + "' đã tồn tại!");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        if (categoryRepository.existsByNameAndIdNot(category.getName(), id)) {
            throw new RuntimeException("Tên thể loại '" + category.getName() + "' đã tồn tại!");
        }
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
