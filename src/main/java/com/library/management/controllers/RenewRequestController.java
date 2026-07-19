package com.library.management.controllers;

import com.library.management.dto.RenewRequestDTO;
import com.library.management.services.RenewRequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/renew-requests")
@RequiredArgsConstructor
public class RenewRequestController {

    private final RenewRequestService renewRequestService;

    @GetMapping("/create/{borrowDetailId}")
    public String showRenewForm(
            @PathVariable Long borrowDetailId,
            Model model
    ) {
        model.addAttribute("borrowDetailId", borrowDetailId);
        model.addAttribute("renewRequest", new RenewRequestDTO());

        return "renew/request-form";
    }

    @PostMapping("/create/{borrowDetailId}")
    public String createRenewRequest(
            @PathVariable Long borrowDetailId,
            @Valid @ModelAttribute("renewRequest") RenewRequestDTO dto,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {
            model.addAttribute("borrowDetailId", borrowDetailId);
            return "renew/request-form";
        }

        String username = (String) session.getAttribute("loggedInUser");

        renewRequestService.createRequest(borrowDetailId, username, dto
        );

        redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu gia hạn thành công"
        );

        return "redirect:/borrows/history";
    }
}