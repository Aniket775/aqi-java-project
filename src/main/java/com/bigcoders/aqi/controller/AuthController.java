package com.bigcoders.aqi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "expired", required = false) String expired,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please verify your credentials.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been successfully logged out.");
        }
        if (expired != null) {
            model.addAttribute("errorMessage", "Your session has expired. Please sign in again.");
        }
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDeniedPage(Model model) {
        model.addAttribute("title", "Access Denied");
        return "access-denied";
    }
}

