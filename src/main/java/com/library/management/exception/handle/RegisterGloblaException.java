package com.library.management.exception.handle;

import com.library.management.controllers.AuthController;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.exception.GmailAlreadyExistException;
import com.library.management.exception.UsernameAlreadyExistException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = AuthController.class)
public class RegisterGloblaException {

    @ExceptionHandler({
            UsernameAlreadyExistException.class,
            GmailAlreadyExistException.class
    })
    public String handleException(RuntimeException ex, Model model) {
        model.addAttribute("registerError", ex.getMessage());
        model.addAttribute("user", new RegisterRequestDTO());
        return "auths/register";
    }
}
