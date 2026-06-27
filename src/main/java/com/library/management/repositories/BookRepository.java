package com.library.management.repositories;

import com.library.management.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book,Integer> {
    public List<Book> findAll();
    public List<Book> findAllByIsDeletedFalse();
}
