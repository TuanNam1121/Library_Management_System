package com.library.management.controllers;

import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.entities.Author;
import com.library.management.entities.Category;
import com.library.management.repositories.AuthorRepository;
import com.library.management.repositories.CategoryRepository;
import com.library.management.services.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    /**
     * UC02 - Search Books: tìm kiếm theo title, author, category với paging
     */
    @GetMapping("")
    public String searchBooks(@ModelAttribute BookSearchDTO searchDTO, Model model) {
        Page<BookReturnDTO> bookPage = bookService.searchBooks(searchDTO);

        List<Category> categories = categoryRepository.findAll();
        List<Author> authors = authorRepository.findAll();

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("categories", categories);
        model.addAttribute("authors", authors);

        // Thông tin paging
        model.addAttribute("currentPage", bookPage.getNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());

        return "books/list";
    }

    /**
     * UC03 - View Book Detail: xem thông tin chi tiết, ảnh bìa, tình trạng sẵn có
     */
    @GetMapping("/{id}")
    public String viewBookDetail(@PathVariable Long id, Model model) {
        try {
            BookReturnDTO book = bookService.getBookById(id);
            model.addAttribute("book", book);
            return "books/detail";
        } catch (EntityNotFoundException e) {
            return "redirect:/books?error=notfound";
        }
    }
}
