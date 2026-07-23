package com.library.management.exception.handle;

import com.library.management.controllers.BookController;
import com.library.management.dto.BookFormDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.ServletRequestDataBinder;
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
    public String handleGeneralException(Exception ex, HttpServletRequest request, RedirectAttributes ra) {
        String errorMessage = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Đã xảy ra lỗi không xác định. Vui lòng thử lại!";

        ra.addFlashAttribute("errorMessage", errorMessage);

        String uri = request.getRequestURI();
        if (uri != null) {
            if (uri.equals("/books/new") || (uri.startsWith("/books/") && uri.endsWith("/edit"))) {
                BookFormDTO form = new BookFormDTO();
                ServletRequestDataBinder binder = new ServletRequestDataBinder(form);
                binder.bind(request);
                ra.addFlashAttribute("form", form);
                return "redirect:" + uri;
            }
        }

        return "redirect:/books";
    }
}
