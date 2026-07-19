package com.library.management.exception.handle;

import com.library.management.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RegisterGloblaException {
    @ExceptionHandler(RuntimeException.class)
    public String handleException(RuntimeException ex, Model model) {
        model.addAttribute("registerError", ex.getMessage());
        model.addAttribute("user",new User());
        return "/auths/register";
    }

}
