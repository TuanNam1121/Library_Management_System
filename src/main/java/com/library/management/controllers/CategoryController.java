package com.library.management.controllers;

import com.library.management.dto.BookReturnDTO;
import com.library.management.dto.BookSearchDTO;
import com.library.management.entities.Category;
import com.library.management.exception.CategoryAlreadyExistsException;
import com.library.management.exception.CategoryException;
import com.library.management.exception.CategoryNotFoundException;
import com.library.management.services.BookService;
import com.library.management.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final BookService bookService;

    @GetMapping("")
    public String listCategories(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Category> categoryPage = categoryService.searchCategories(name, page);

        model.addAttribute("categories", categoryPage);
        model.addAttribute("searchName", name);
        model.addAttribute("currentPage", categoryPage.getNumber());
        model.addAttribute("pageSize", categoryPage.getSize());
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        return "categories/list";
    }

    @GetMapping("/{id}/books")
    public String viewBooksByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        try {
            Category category = categoryService.getCategoryById(id);

            BookSearchDTO searchDTO = new BookSearchDTO();
            searchDTO.setCategoryId(id);
            Page<BookReturnDTO> bookPage = bookService.searchBooks(searchDTO);

            model.addAttribute("category", category);
            model.addAttribute("books", bookPage.getContent());
            model.addAttribute("currentPage", bookPage.getNumber());
            model.addAttribute("totalPages", bookPage.getTotalPages());
            model.addAttribute("totalItems", bookPage.getTotalElements());
            return "categories/books";
        } catch (CategoryNotFoundException exception) {
            return "redirect:/categories?error=notfound";
        }
    }

    //create
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "categories/form";
    }

    @PostMapping("/create")
    public String createCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category);
            return "redirect:/categories?success=true";
        } catch (CategoryAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories/create";
        }
    }

    //edit
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Category category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            model.addAttribute("isEdit", true);
            return "categories/form";
        } catch (CategoryNotFoundException exception) {
            return "redirect:/categories?error=notfound";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, category);
            return "redirect:/categories?updated=true";
        } catch (CategoryException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories/edit/" + id;
        }
    }

    //delete
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xóa thể loại và các sách thuộc thể loại thành công."
            );
        } catch (CategoryException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/categories";
    }
}
