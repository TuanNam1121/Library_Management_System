package com.library.management.controllers;

import com.library.management.dto.ChangePasswordDTO;
import com.library.management.dto.UpdateProfileDTO;
import com.library.management.entities.User;
import com.library.management.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/auths/login";

        User user = userService.getByUsername(username);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());

        model.addAttribute("user", user);
        model.addAttribute("updateProfileDTO", dto);
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());

        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(HttpSession session,
                                @Valid @ModelAttribute("updateProfileDTO") UpdateProfileDTO dto,
                                BindingResult bindingResul ,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/auths/login";
        if (bindingResul.hasErrors()) {
            User user = userService.getByUsername(username);
            model.addAttribute("user", user);
            model.addAttribute("updateProfileDTO", dto);
            model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
            return "profile/index";
        }
        try {
            userService.updateProfile(username, dto);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(HttpSession session,
                                 @Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/auths/login";

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("passwordError", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile";
        }
        if(dto.getNewPassword().equals(userService.getByUsername(username).getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError",
                    "Mật khẩu mới phải khác mật khẩu cũ!");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(username, dto);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
