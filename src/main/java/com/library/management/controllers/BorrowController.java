package com.library.management.controllers;

import com.library.management.entities.BorrowDetail;
import com.library.management.entities.BorrowRequest;
import com.library.management.enums.BorrowStatus;
import com.library.management.services.BorrowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/request/{bookId}")
    public String submitBorrowRequest(@PathVariable Long bookId,
                                      HttpSession session,
                                      RedirectAttributes ra) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loggedInUser == null) {
            return "redirect:/auths/login";
        }
        if (!"READER".equals(userRole)) {
            ra.addFlashAttribute("borrowError", "Chỉ có Độc giả mới có quyền mượn sách");
            return "redirect:/books/" + bookId;
        }

        borrowService.createBorrowRequest(loggedInUser, bookId);
        ra.addFlashAttribute("borrowSuccess", true);
        return "redirect:/books/" + bookId;
    }

    @GetMapping("/history")
    public String viewBorrowHistory(HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"READER".equals(userRole)) {
            return "redirect:/books";
        }

        List<BorrowRequest> history = borrowService.getBorrowHistory(loginUser);
        model.addAttribute("requests", history);
        return "borrows/history";
    }

    @GetMapping("/overdue")
    public String viewOverdueBooks(HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"READER".equals(userRole)) {
            return "redirect:/books";
        }

        List<BorrowDetail> overdueBooks = borrowService.getOverdueBooks(loginUser);
        model.addAttribute("overdueBooks", overdueBooks);
        return "borrows/overdue";
    }

    @GetMapping("/requests")
    public String viewPendingRequests(HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        List<BorrowRequest> requests = borrowService.getPendingRequests();
        model.addAttribute("requests", requests);
        return "borrows/requests";
    }

    @GetMapping("/management")
    public String viewAllRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BorrowStatus status,
            HttpSession session,
            Model model
    ) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        List<BorrowRequest> requests = borrowService.searchRequests(keyword, status);
        model.addAttribute("requests", requests);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("selectedStatus", status);
        return "borrows/management";
    }

    @GetMapping("/{id}")
    public String viewBorrowDetails(@PathVariable Long id, HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }

        // BorrowOperationException sẽ được BorrowGlobalException bắt nếu không tìm thấy
        BorrowRequest request = borrowService.getRequestById(id);

        // Security check: reader can only see their own requests
        if ("READER".equals(userRole) && !request.getReader().getUsername().equals(loginUser)) {
            return "redirect:/books";
        }

        // Tính số giây còn lại của thời gian giữ chỗ (server-side)
        if (request.getStatus() == BorrowStatus.RESERVED
                && request.getDetails() != null
                && !request.getDetails().isEmpty()
                && request.getDetails().get(0).getReservedAt() != null) {
            LocalDateTime reservedAt = request.getDetails().get(0).getReservedAt();
            LocalDateTime expireAt = reservedAt.plusMinutes(1);
            long secondsLeft = Duration.between(LocalDateTime.now(), expireAt).getSeconds();
            model.addAttribute("reservationSecondsLeft", secondsLeft);
        } else {
            model.addAttribute("reservationSecondsLeft", null);
        }

        model.addAttribute("request", request);
        return "borrows/detail";
    }

    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        borrowService.approveRequest(id, loginUser);
        ra.addFlashAttribute("borrowSuccess", "approved");
        return "redirect:/borrows/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes ra) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        borrowService.rejectRequest(id, loginUser);
        ra.addFlashAttribute("borrowSuccess", "rejected");
        return "redirect:/borrows/" + id;
    }

    @PostMapping("/{id}/confirm-pickup")
    public String confirmPickup(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes ra) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        borrowService.confirmPickup(id, loginUser);
        ra.addFlashAttribute("borrowSuccess", "pickupConfirmed");
        return "redirect:/borrows/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        borrowService.cancelReservation(id, loginUser);
        ra.addFlashAttribute("borrowSuccess", "reservationCancelled");
        return "redirect:/borrows/" + id;
    }

    @PostMapping("/{id}/return")
    public String recordReturn(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes ra) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        boolean hasFine = borrowService.recordReturn(id);
        if (hasFine) {
            ra.addFlashAttribute("borrowSuccess", "returnedWithFine");
        } else {
            ra.addFlashAttribute("borrowSuccess", "returned");
        }
        return "redirect:/borrows/" + id;
    }

    @PostMapping("/{id}/pay-fine")
    public String payFine(@PathVariable Long id,
                          HttpSession session,
                          RedirectAttributes ra) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        borrowService.payFine(id);
        ra.addFlashAttribute("borrowSuccess", "paid");
        return "redirect:/borrows/" + id;
    }
}
