package com.library.management.exception.handle;

import com.library.management.controllers.ProfileController;
import com.library.management.exception.CanNotSaveAvartaException;
import com.library.management.exception.WrongComfirmPasswordException;
import com.library.management.exception.WrongCurrentPasswordException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = ProfileController.class)
public class ChangePassWordGlobalHandleException {

    @ExceptionHandler({
            WrongCurrentPasswordException.class,
            WrongComfirmPasswordException.class
    })
    public String handlePasswordException(
            RuntimeException ex,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        return "redirect:/profile";
    }

    @ExceptionHandler(CanNotSaveAvartaException.class)
    public String handleAvatarException(
            CanNotSaveAvartaException ex,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        return "redirect:/profile";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "errorMsg",
                "Ảnh đại diện không được vượt quá 5MB."
        );
        return "redirect:/profile";
    }
}
