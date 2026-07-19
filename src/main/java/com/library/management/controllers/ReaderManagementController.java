package com.library.management.controllers;

import com.library.management.entities.User;
import com.library.management.services.BorrowService;
import com.library.management.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/readers")
@RequiredArgsConstructor
public class ReaderManagementController {
    private final UserService userService;
    private final BorrowService borrowService;

    @GetMapping("")
    public String list(@RequestParam(name = "keyword", required = false) String keyword, Model model) {

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("readers", userService.searchReader(keyword));
        } else {
            model.addAttribute("readers", userService.findAllReader());
        }
        model.addAttribute("keyword", keyword);

        return "readers/list";
    }

    @GetMapping("/deactivate/{id}")
    public String deactivate(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setEnabled(false);
        userService.update(user);
        return "redirect:/readers";
    }

    @GetMapping("/activate/{id}")
    public String activate(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setEnabled(true);
        userService.update(user);
        return "redirect:/readers";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("reader", user);
        model.addAttribute("borrow", borrowService.findHistoryByReader(id));
        return "readers/view";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("reader", user);
        return "readers/edit";
    }

    @PostMapping("/edit")
    public String editPost(@ModelAttribute("reader")User user){
        userService.update(user);
        return "redirect:/readers";
    }
}
