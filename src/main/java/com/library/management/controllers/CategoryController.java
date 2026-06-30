package com.library.management.controllers;

import com.library.management.entities.Category;
import com.library.management.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    //create
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/create";
    }

    @PostMapping("/create")
    public String createCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category);
            return "redirect:/categories?success=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories/create";
        }
    }

    //edit
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "categories/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, category);
            return "redirect:/categories?updated=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories/edit/" + id;
        }
    }

    //delete
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories?deleted=true";
    }
}
