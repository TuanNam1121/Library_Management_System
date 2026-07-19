package com.library.management.controllers;

import com.library.management.entities.RenewRequest;
import com.library.management.enums.RenewStatus;
import com.library.management.repositories.RenewRequestRepository;
import com.library.management.services.RenewRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/librarian/renew")
@RequiredArgsConstructor
public class LibrarianRenewRequestController {

    private final RenewRequestRepository renewRequestRepository;
    private final RenewRequestService renewRequestService;

    @GetMapping
    public String pendingRequests(
            HttpSession session,
            Model model
    ) {

        String role = (String) session.getAttribute("userRole");

        if (!"LIBRARIAN".equals(role)) {
            return "redirect:/books";
        }

        List<RenewRequest> requests =
                renewRequestRepository.findByStatusOrderByRequestedAtAsc(RenewStatus.PENDING);

        model.addAttribute("requests", requests);

        return "librarians/renew-list";
    }

    @PostMapping("/{renewRequestId}/approve")
    public String approve(
            @PathVariable Long renewRequestId,
            @RequestParam(required = false)
            String librarianNote,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        String username = (String) session.getAttribute("loggedInUser");

        renewRequestService.approve(renewRequestId, username, librarianNote);

        redirectAttributes.addFlashAttribute("success", "Duyệt yêu cầu gia hạn thành công");
        return "redirect:/librarian/renew";
    }

    @PostMapping("/{renewRequestId}/reject")
    public String reject(
            @PathVariable Long renewRequestId,
            @RequestParam(required = false)
            String librarianNote,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        String username = (String) session.getAttribute("loggedInUser");

        renewRequestService.reject(renewRequestId, username, librarianNote);

        redirectAttributes.addFlashAttribute("success", "Đã từ chối yêu cầu"
        );
        return "redirect:/librarian/renew";
    }
}