package com.library.management.exception.handle;

import com.library.management.controllers.RenewRequestController;
import com.library.management.exception.RenewBusinessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = RenewRequestController.class)
public class RenewRequestGlobalException {

    @ExceptionHandler(RenewBusinessException.class)
    public String handleRenewException(
            RenewBusinessException ex,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(
                "renewError",
                ex.getMessage()
        );

        return "redirect:/renew-requests/create/"
                + ex.getBorrowDetailId();
    }
}
