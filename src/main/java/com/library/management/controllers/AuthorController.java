package com.library.management.controllers;

import com.library.management.entities.Author;
import com.library.management.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public String list(Model model){

        model.addAttribute("authors", authorService.findAll());

        return "authors/list";
    }

    @GetMapping("/create")
    public String create(Model model){

        model.addAttribute("author", new Author());

        return "authors/create";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Author author){

        authorService.save(author);

        return "redirect:/authors";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       Model model){

        model.addAttribute(
                "author",
                authorService.findById(id)
        );

        return "authors/detail";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id){

        authorService.delete(id);

        return "redirect:/authors";
    }


}
