package com.library.management.exception.handle;

import com.library.management.controllers.AuthController;
import com.library.management.dto.LoginRequestDTO;
import com.library.management.exception.UserNotFoundException;
import com.library.management.exception.UsernameNotExistException;
import com.library.management.exception.WrongPasswordOrUserNameException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = AuthController.class)
public class LoginGlobalException {

    @ExceptionHandler({
            UsernameNotExistException.class,
            WrongPasswordOrUserNameException.class,
            UserNotFoundException.class

    })
    public String handleException(RuntimeException ex, Model model) {
        model.addAttribute("loginError", ex.getMessage());
        model.addAttribute("loginRequestDto", new LoginRequestDTO());
        return "auths/login";
    }
}
