package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.entity.User;
import com.cybersecurity.cybersecurity_info_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // === REGISTRATION FORM ===
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    // === SUBMIT REGISTRATION ===
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes,
            Model model) {

        boolean success = userService.registerUser(user);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Username or Email already exists!");
            model.addAttribute("user", user); // Preserve input
            return "register";
        }
    }

    // === LOGIN PAGE ===
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}