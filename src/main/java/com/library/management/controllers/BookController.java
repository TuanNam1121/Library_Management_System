package com.library.management.controllers;

import com.library.management.dto.BookReturnDTO;
import com.library.management.services.BookService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("")
    public ModelAndView viewAllBook(){
        List<BookReturnDTO> list = bookService.getAll();
        ModelAndView mv = new ModelAndView();
        mv.setViewName("books/list");
        mv.addObject("list", list);
        return mv;
    }
}
