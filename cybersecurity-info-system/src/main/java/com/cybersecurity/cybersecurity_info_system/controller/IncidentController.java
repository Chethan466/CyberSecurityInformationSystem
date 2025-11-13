package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.entity.Incident;
import com.cybersecurity.cybersecurity_info_system.entity.User;
import com.cybersecurity.cybersecurity_info_system.repository.IncidentRepository;
import com.cybersecurity.cybersecurity_info_system.repository.UserRepository;
import com.cybersecurity.cybersecurity_info_system.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/incidents")
public class IncidentController {

    @Autowired private IncidentRepository incidentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AlertService alertService;

    // === LIST ALL INCIDENTS (VIEWER ALLOWED) ===
    @GetMapping
    public String list(Model model, Authentication auth) {
        List<Incident> incidents = incidentRepository.findAll();
        model.addAttribute("incidents", incidents);

        // Only show form to ADMIN/ANALYST
        if (hasRole(auth, "ADMIN") || hasRole(auth, "ANALYST")) {
            if (!model.containsAttribute("newIncident")) {
                model.addAttribute("newIncident", new Incident());
            }
            model.addAttribute("analysts", userRepository.findByRole(User.Role.ANALYST));
        }

        return "incidents";
    }

    // === CREATE NEW INCIDENT (ADMIN + ANALYST ONLY) ===
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public String add(@Valid @ModelAttribute("newIncident") Incident incident,
                      BindingResult result,
                      Authentication auth,
                      RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.newIncident", result);
            ra.addFlashAttribute("newIncident", incident);
            ra.addFlashAttribute("error", "Please fix the errors in the form.");
            return "redirect:/incidents";
        }

        User reporter = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB"));

        incident.setReportedBy(reporter);
        incident.setStatus(Incident.Status.OPEN);

        incidentRepository.save(incident);

        // CREATE ALERT
        alertService.createIncidentAlert(incident.getId(), incident.getTitle(), incident.getSeverity());

        ra.addFlashAttribute("message", "Incident reported successfully.");
        return "redirect:/incidents";
    }

    // === SHOW EDIT FORM (ADMIN + ANALYST ONLY) ===
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Incident incident = incidentRepository.findById(id).orElse(null);

        if (incident == null) {
            ra.addFlashAttribute("error", "Incident not found.");
            return "redirect:/incidents";
        }

        model.addAttribute("incident", incident);
        model.addAttribute("analysts", userRepository.findByRole(User.Role.ANALYST));
        return "edit-incident";
    }

    // === UPDATE INCIDENT (ADMIN + ANALYST ONLY) ===
    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("incident") Incident updated,
                         BindingResult result,
                         Authentication auth,
                         RedirectAttributes ra) {

        Incident incident = incidentRepository.findById(id).orElse(null);
        if (incident == null) {
            ra.addFlashAttribute("error", "Incident not found.");
            return "redirect:/incidents";
        }

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.incident", result);
            ra.addFlashAttribute("incident", updated);
            return "redirect:/incidents/edit/" + id;
        }

        updated.setReportedBy(incident.getReportedBy());

        incident.setTitle(updated.getTitle());
        incident.setDescription(updated.getDescription());
        incident.setSeverity(updated.getSeverity());
        incident.setStatus(updated.getStatus());
        incident.setAssignedTo(updated.getAssignedTo());
        incident.setResolutionNotes(updated.getResolutionNotes());

        if (updated.getStatus() == Incident.Status.RESOLVED || updated.getStatus() == Incident.Status.CLOSED) {
            if (incident.getResolvedAt() == null) {
                incident.setResolvedAt(LocalDateTime.now());
            }
        } else {
            incident.setResolvedAt(null);
        }

        incidentRepository.save(incident);
        ra.addFlashAttribute("message", "Incident updated successfully.");
        return "redirect:/incidents";
    }

    // === DELETE INCIDENT (ADMIN + ANALYST ONLY) ===
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        if (!incidentRepository.existsById(id)) {
            ra.addFlashAttribute("error", "Incident not found.");
            return "redirect:/incidents";
        }

        try {
            incidentRepository.deleteById(id);
            ra.addFlashAttribute("message", "Incident deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete: Incident is referenced.");
        }
        return "redirect:/incidents";
    }

    // === HELPER: Check role ===
    private boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}