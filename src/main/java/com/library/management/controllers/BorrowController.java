package com.library.management.controllers;

import com.library.management.dto.LoginedUserDTO;
import com.library.management.entities.BorrowDetail;
import com.library.management.entities.BorrowRequest;
import com.library.management.services.BorrowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // UC04 - Reader Submit Borrow Request
    @PostMapping("/request/{bookId}")
    public String submitBorrowRequest(@PathVariable Long bookId, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loggedInUser == null) {
            return "redirect:/auths/login";
        }
        if (!"READER".equals(userRole)) {
            return "redirect:/books/" + bookId + "?borrowError=" + URLEncoder.encode("Chỉ có Độc giả mới có quyền mượn sách", StandardCharsets.UTF_8);
        }

        try {
            borrowService.createBorrowRequest(loggedInUser, bookId);
            return "redirect:/books/" + bookId + "?borrowSuccess=true";
        } catch (RuntimeException ex) {
            return "redirect:/books/" + bookId + "?borrowError=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // UC05 - Reader View Borrow History
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

    // UC05b - Reader View Overdue Books
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

    // UC06 - Librarian View Pending Borrow Requests
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

    // UC06 - Librarian View All Borrow Requests
    @GetMapping("/management")
    public String viewAllRequests(HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        List<BorrowRequest> requests = borrowService.getAllRequests();
        model.addAttribute("requests", requests);
        return "borrows/management";
    }

    // UC06 - Librarian View Borrow Details
    @GetMapping("/{id}")
    public String viewBorrowDetails(@PathVariable Long id, HttpSession session, Model model) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }

        BorrowRequest request = borrowService.getRequestById(id);
        
        // Security check: reader can only see their own requests
        if ("READER".equals(userRole) && !request.getReader().getUsername().equals(loginUser)) {
            return "redirect:/books";
        }

        model.addAttribute("request", request);
        return "borrows/detail";
    }

    // UC06 - Librarian Approve Request
    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id, HttpSession session) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        try {
            borrowService.approveRequest(id, loginUser);
            return "redirect:/borrows/" + id + "?success=approved";
        } catch (RuntimeException ex) {
            return "redirect:/borrows/" + id + "?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // UC06 - Librarian Reject Request
    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id, HttpSession session) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        try {
            borrowService.rejectRequest(id, loginUser);
            return "redirect:/borrows/" + id + "?success=rejected";
        } catch (RuntimeException ex) {
            return "redirect:/borrows/" + id + "?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // UC06 - Librarian Confirm Pickup
    @PostMapping("/{id}/confirm-pickup")
    public String confirmPickup(@PathVariable Long id, HttpSession session) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        try {
            borrowService.confirmPickup(id, loginUser);
            return "redirect:/borrows/" + id + "?success=pickupConfirmed";
        } catch (RuntimeException ex) {
            return "redirect:/borrows/" + id + "?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // UC06 - Librarian Cancel Reservation
    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, HttpSession session) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        try {
            borrowService.cancelReservation(id, loginUser);
            return "redirect:/borrows/" + id + "?success=reservationCancelled";
        } catch (RuntimeException ex) {
            return "redirect:/borrows/" + id + "?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // UC07 - Librarian Record Return
    @PostMapping("/{id}/return")
    public String recordReturn(@PathVariable Long id, HttpSession session) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        try {
            boolean hasFine = borrowService.recordReturn(id);
            if (hasFine) {
                // Has unpaid fine — stay on page to show fine info and payment button
                return "redirect:/borrows/" + id + "?success=returnedWithFine";
            }
            return "redirect:/borrows/" + id + "?success=returned";
        } catch (RuntimeException ex) {
            return "redirect:/borrows/" + id + "?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // UC07 - Librarian Confirm Payment
    @PostMapping("/{id}/pay-fine")
    public String payFine(@PathVariable Long id, HttpSession session) {
        String loginUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (loginUser == null) {
            return "redirect:/auths/login";
        }
        if (!"LIBRARIAN".equals(userRole)) {
            return "redirect:/books";
        }

        try {
            borrowService.payFine(id);
            return "redirect:/borrows/" + id + "?success=paid";
        } catch (RuntimeException ex) {
            return "redirect:/borrows/" + id + "?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
