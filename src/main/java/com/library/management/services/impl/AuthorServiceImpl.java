package com.library.management.services.impl;

import com.library.management.entities.Author;
import com.library.management.entities.Book;
import com.library.management.repositories.AuthorRepository;
import com.library.management.repositories.BookRepository;
import com.library.management.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Override
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Override
    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElse(null);
    }

    @Override
    public Author save(Author author) {
        return authorRepository.save(author);
    }

    @Override
    public void delete(Long id) {
        Book book = bookRepository.existAuthor(id);
        if(book == null){
            authorRepository.deleteById(id);
        }
    }

    @Override
    public List<Author> searchByName(String keyword) {
        return authorRepository.searchByName(keyword);
    }

}
