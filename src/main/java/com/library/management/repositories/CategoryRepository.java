package com.library.management.repositories;

import com.library.management.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    Page<Category> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Category> findByIdAndIsDeletedFalse(Long id);
    List<Category> findAllByIsDeletedFalseOrderByNameAsc();
}
