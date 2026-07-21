package com.library.management.controllers;

import com.library.management.entities.Role;
import com.library.management.entities.User;
import com.library.management.services.BorrowService;
import com.library.management.services.RoleService;
import com.library.management.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Controller
@RequestMapping("/readers")
@RequiredArgsConstructor
public class ReaderManagementController {
    private final UserService userService;
    private final BorrowService borrowService;
    private final RoleService roleService;

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
        System.out.println("this is date"+user.getDob());
        model.addAttribute("reader", user);
        model.addAttribute("birth", user.getDob());
        return "readers/edit";
    }

    @PostMapping("/edit")
    public String editPost(@ModelAttribute("reader")User user,
                           @RequestParam("avatarFile") MultipartFile avatarFile,
                           @RequestParam("role_id")long role_id,
                           @RequestParam(value = "birth", required = false)LocalDate birth,
                           Model model) throws IOException {

        if(!userService.searchUser(user, userService.findById(user.getId()))){
            model.addAttribute("error", "email/phone existed");
            model.addAttribute("reader", user);
            model.addAttribute("birth", user.getDob());
            return "readers/edit";
        }

        if (!avatarFile.isEmpty()) {

            // Create uploads folder if it doesn't exist
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Optional: make filename unique
            String fileName = System.currentTimeMillis() + "_"
                    + avatarFile.getOriginalFilename();

            // Save file
            avatarFile.transferTo(uploadDir.resolve(fileName));

            // Save URL into database
            user.setAvatar("/uploads/" + fileName);
        }
        Role role = roleService.findById(role_id);
        user.setRole(role);
        user.setDob(birth);
        userService.update(user);
        return "redirect:/readers";
    }
}
