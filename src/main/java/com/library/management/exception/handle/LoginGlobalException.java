package com.library.management.exception.handle;

import com.library.management.dto.LoginRequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LoginGlobalException {
    @ExceptionHandler(RuntimeException.class)
    public String handleException(RuntimeException ex, Model model) {
        model.addAttribute("loginError", ex.getMessage());
        model.addAttribute("loginRequestDto", new LoginRequestDTO());
        return"/auths/login";


    }
}
