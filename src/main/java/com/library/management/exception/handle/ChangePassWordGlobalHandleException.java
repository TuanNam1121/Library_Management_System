package com.library.management.exception.handle;

import com.library.management.controllers.ProfileController;
import com.library.management.exception.CanNotSaveAvartaException;
import com.library.management.exception.WrongComfirmPasswordException;
import com.library.management.exception.WrongCurrentPasswordException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = ProfileController.class)
public class ChangePassWordGlobalHandleException {

    @ExceptionHandler({
            WrongCurrentPasswordException.class,
            WrongComfirmPasswordException.class,
            CanNotSaveAvartaException.class
    })
    public String handleException(
            RuntimeException ex,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        return "redirect:/profile";
    }
}
