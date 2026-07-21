package com.library.management.repositories;

import com.library.management.entities.Book;
import com.library.management.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findAll();

    List<Book> findAllByIsDeletedFalse();

    List<Book> findAllByCategoryIdAndIsDeletedFalse(Long categoryId);

    Optional<Book> findByIdAndIsDeletedFalseAndStatus(Long id, BookStatus status);

    @Query("""
            SELECT b FROM Book b
            WHERE (b.isDeleted = false OR b.isDeleted IS NULL)
              AND b.status = com.library.management.enums.BookStatus.APPROVED
              AND (CAST(:keyword AS string) IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
              AND (:authorId IS NULL OR b.author.id = :authorId)
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
            """)
    Page<Book> searchBooks(@Param("keyword") String keyword, @Param("authorId") Long authorId, @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("FROM Book b where b.author.id = :authorID")
    Book existAuthor(@Param("authorID") Long id);

    @Query("FROM Book b where b.author.id = :authorID")
    List<Book> findByAuthor(@Param("authorID") Long id);
}
