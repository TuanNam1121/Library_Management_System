package com.library.management.exception.handle;

import com.library.management.controllers.BorrowController;
import com.library.management.exception.BorrowOperationException;
import com.library.management.exception.BorrowSubmitException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler cho BorrowController.
 * Dùng flash attribute để hiển thị lỗi trực tiếp trên màn hình
 * mà không cần thêm query param vào URL.
 */
@ControllerAdvice(assignableTypes = BorrowController.class)
public class BorrowGlobalException {

    @ExceptionHandler(BorrowOperationException.class)
    public String handleBorrowOperation(BorrowOperationException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("borrowError", ex.getMessage());
        return "redirect:/borrows/" + ex.getRequestId();
    }

    @ExceptionHandler(BorrowSubmitException.class)
    public String handleBorrowSubmit(BorrowSubmitException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("borrowError", ex.getMessage());
        return "redirect:/books/" + ex.getBookId();
    }
}
