package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.entity.User;
import com.cybersecurity.cybersecurity_info_system.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model, Authentication auth) {
        User user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "edit-profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String email,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Authentication auth,
            RedirectAttributes ra) {

        User user = getCurrentUser(auth);
        if (user == null) return "redirect:/login";

        // === VALIDATE EMAIL ===
        if (!StringUtils.hasText(email) || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            ra.addFlashAttribute("error", "Valid email is required.");
            return "redirect:/profile/edit";
        }

        // === UPDATE EMAIL ===
        user.setEmail(email.trim());

        // === UPDATE PASSWORD (if provided) ===
        if (StringUtils.hasText(newPassword)) {
            if (!newPassword.equals(confirmPassword)) {
                ra.addFlashAttribute("error", "Passwords do not match.");
                return "redirect:/profile/edit";
            }
            if (newPassword.length() < 6) {
                ra.addFlashAttribute("error", "Password must be at least 6 characters.");
                return "redirect:/profile/edit";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepo.save(user);
        ra.addFlashAttribute("message", "Profile updated successfully.");
        return "redirect:/profile";
    }

    // HELPER
    private User getCurrentUser(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            return userRepo.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
    }
}