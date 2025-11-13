package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.entity.Alert;
import com.cybersecurity.cybersecurity_info_system.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alerts")
@PreAuthorize("hasRole('ADMIN') OR hasRole('ANALYST')")
public class AlertController {

    @Autowired private AlertRepository alertRepo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("alerts", alertRepo.findTop20ByOrderByCreatedAtDesc());
        model.addAttribute("unreadCount", alertRepo.countByStatus(Alert.Status.UNREAD));
        return "alerts";
    }

    @PostMapping("/read/{id}")
    public String markRead(@PathVariable Long id, RedirectAttributes redirect) {
        alertRepo.findById(id).ifPresent(alert -> {
            alert.setStatus(Alert.Status.READ);
            alertRepo.save(alert);
        });
        return "redirect:/alerts";
    }

    @PostMapping("/resolve/{id}")
    public String resolve(@PathVariable Long id, RedirectAttributes redirect) {
        alertRepo.findById(id).ifPresent(alert -> {
            alert.setStatus(Alert.Status.RESOLVED);
            alertRepo.save(alert);
        });
        return "redirect:/alerts";
    }
}