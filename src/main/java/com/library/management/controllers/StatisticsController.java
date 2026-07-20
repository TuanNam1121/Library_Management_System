package com.library.management.controllers;

import com.library.management.services.StatisticsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public String getStatistics(HttpSession session, Model model) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null) {
            return "redirect:/auths/login";
        }
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/books";
        }

        Map<String, Object> stats = statisticsService.getSimpleStatistics();
        model.addAllAttributes(stats);

        return "statistics/index";
    }
}
