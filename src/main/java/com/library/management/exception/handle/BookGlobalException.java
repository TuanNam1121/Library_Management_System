package com.library.management.exception.handle;

import com.library.management.controllers.BookController;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = BookController.class)
public class BookGlobalException {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Không tìm thấy sách.");
        return "redirect:/books";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Lỗi: " + ex.getMessage());
        return "redirect:/books";
    }
}
