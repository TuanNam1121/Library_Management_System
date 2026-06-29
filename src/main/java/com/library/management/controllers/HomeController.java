package com.library.management.controllers;

import com.library.management.entities.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class HomeController {

    @GetMapping
    public String homepage(Model model, HttpSession session){
        if(session.getAttribute("loggedInUser") == null) return "redirect:auths/login";
        return "redirect:books/";
    }

}
