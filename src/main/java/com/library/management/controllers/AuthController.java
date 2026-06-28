package com.library.management.controllers;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.entities.User;
import com.library.management.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public String loginProcess(Model model) {
        model.addAttribute("user", new LoginRequestDTO());
        return "auths/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("user") LoginRequestDTO dto,
                        BindingResult result,
                        HttpSession session) {
        if (result.hasErrors()) {
            return "auths/login";
        }
        try {
            User user = userService.login(dto);


            if (!dto.getUsername().equals(user.getUsername()) || !dto.getPassword().equals(user.getPassword())) {
                result.reject("loginError", "Invalid username or password");
                return "auths/login";
            }


            // Lưu thông tin vào session
            session.setAttribute("loggedInUser", user.getUsername());
            session.setAttribute("userRole", user.getRole() != null ? user.getRole().getName() : "READER");


        } catch (RuntimeException ex) {
            result.reject("loginError", ex.getMessage());
            return "auths/login";
        }

        // Redirect theo role
        String role = (String) session.getAttribute("userRole");
        if (role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("LIBRARIAN"))) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/books";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auths/login";
    }
}
