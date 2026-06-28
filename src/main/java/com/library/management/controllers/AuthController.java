package com.library.management.controllers;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auths")
@RequiredArgsConstructor

public class AuthController {
private final UserService userService;

    @GetMapping("/register")
    public String registerProcess(Model model) {
        model.addAttribute("user", new RegisterRequestDTO());
        return "auths/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") RegisterRequestDTO dto,
            BindingResult result) {

        if (result.hasErrors()) {
            return "auths/register";
        }

        try {
            userService.register(dto);
        } catch (RuntimeException ex) {
            result.reject("registerError", ex.getMessage());
            return "auths/register";
        }

        return "redirect:/auths/login";
    }


    @GetMapping("/login")
    public String loginProcess(Model model){
        model.addAttribute("user",new  LoginRequestDTO());
        return "auths/login";
    }
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("user") LoginRequestDTO loginRequestDTO,
                        BindingResult result,
                        Model model,
                        jakarta.servlet.http.HttpSession session){
        if(result.hasErrors()){
            return "auths/login";
        }
        try {
            boolean success = userService.login(loginRequestDTO);

            if (!success) {
                result.reject("loginError", "Invalid username or password");
                return "auths/login";
            }

            // Save user to session
            session.setAttribute("loggedInUser", loginRequestDTO.getUsername());

        } catch (RuntimeException ex) {
            result.reject("loginError", ex.getMessage());
            return "auths/login";
        }
        return "redirect:/books";
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/auths/login";
    }
    }


