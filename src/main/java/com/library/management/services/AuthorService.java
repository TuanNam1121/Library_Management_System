package com.library.management.services;

import com.library.management.entities.Author;

import java.util.List;

public interface AuthorService {

    List<Author> findAll();

    Author findById(Long id);

    Author save(Author author);

    void delete(Long id);

    List<Author> searchByName(String keyword);

}
