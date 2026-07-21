package com.library.management.controllers;

import com.library.management.dto.BookFormDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    @GetMapping("")
    public String searchBooks(@ModelAttribute BookSearchDTO searchDTO, Model model) {
        Page<BookReturnDTO> bookPage = bookService.searchBooks(searchDTO);

        List<Category> categories = categoryRepository.findAllByIsDeletedFalseOrderByNameAsc();
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

    @GetMapping("/new")
    public String showAddForm(Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/books?error=unauthorized";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BookFormDTO());
        }
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("authors", authorRepository.findAll());
        return "books/add";
    }

    @PostMapping("/new")
    public String createBook(@ModelAttribute BookFormDTO form, RedirectAttributes ra, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/books?error=unauthorized";
        }
        try {
            BookReturnDTO created = bookService.createBook(form);
            ra.addFlashAttribute("successMessage", "Đã thêm sách \"" + created.getTitle() + "\" thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi thêm sách: " + e.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/books/new";
        }
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/books?error=unauthorized";
        }
        try {
            BookReturnDTO book = bookService.getBookByIdForAdmin(id);
            if (!model.containsAttribute("form")) {
                BookFormDTO form = new BookFormDTO();
                form.setTitle(book.getTitle());
                form.setIsbn(book.getIsbn());
                form.setDescription(book.getDescription());
                form.setQuantity(book.getQuantity());
                form.setAvailableQuantity(book.getAvailableQuantity());
                form.setCategoryId(book.getCategoryId());
                form.setAuthorId(book.getAuthorId());
                model.addAttribute("form", form);
            }

            model.addAttribute("book", book);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("authors", authorRepository.findAll());
            return "books/edit";
        } catch (EntityNotFoundException e) {
            return "redirect:/books?error=notfound";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateBook(@PathVariable Long id, @ModelAttribute BookFormDTO form, RedirectAttributes ra, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/books?error=unauthorized";
        }
        try {
            BookReturnDTO updated = bookService.updateBook(id, form);
            ra.addFlashAttribute("successMessage", "Đã cập nhật sách \"" + updated.getTitle() + "\"!");
            return "redirect:/books/" + id;
        } catch (EntityNotFoundException e) {
            return "redirect:/books?error=notfound";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/books/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/books?error=unauthorized";
        }
        try {
            bookService.deleteBook(id);
            ra.addFlashAttribute("successMessage", "Đã xóa sách thành công.");
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy sách.");
        }
        return "redirect:/books";
    }
}
