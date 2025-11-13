package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.entity.Asset;
import com.cybersecurity.cybersecurity_info_system.entity.User;
import com.cybersecurity.cybersecurity_info_system.repository.AssetRepository;
import com.cybersecurity.cybersecurity_info_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository; // ADDED HERE

    // === USER MANAGEMENT ===

    // List all users
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users"; // templates/admin/users.html
    }

    // Edit user role
    @PostMapping("/users/edit/{id}")
    @Transactional
    public String editUserRole(@PathVariable Long id, @RequestParam("role") String roleStr, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            User.Role role = User.Role.valueOf(roleStr.toUpperCase());
            user.setRole(role);
            userRepository.save(user);
            ra.addFlashAttribute("message", "User role updated to " + role + " successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Invalid role selected. Use ADMIN, ANALYST, or VIEWER.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update role. Please try again.");
        }

        return "redirect:/admin/users";
    }

    // === ASSET MANAGEMENT ===

    // List all assets
    @GetMapping("/assets")
    public String listAssets(Model model) {
        List<Asset> assets = assetRepository.findAll();
        model.addAttribute("assets", assets);
        model.addAttribute("newAsset", new Asset()); // For add form
        return "admin/assets"; // templates/admin/assets.html
    }

    // Add new asset
    @PostMapping("/assets/add")
    public String addAsset(@ModelAttribute("newAsset") Asset asset, RedirectAttributes ra) {
        if (assetRepository.existsByAssetName(asset.getAssetName())) {
            ra.addFlashAttribute("error", "Asset name already exists.");
            return "redirect:/admin/assets";
        }
        assetRepository.save(asset);
        ra.addFlashAttribute("message", "Asset added successfully.");
        return "redirect:/admin/assets";
    }

    // Edit asset
    @PostMapping("/assets/edit/{id}")
    public String editAsset(@PathVariable Long id, @ModelAttribute Asset updatedAsset, RedirectAttributes ra) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        asset.setAssetName(updatedAsset.getAssetName());
        asset.setAssetType(updatedAsset.getAssetType());
        asset.setIpAddress(updatedAsset.getIpAddress());
        asset.setDescription(updatedAsset.getDescription());

        assetRepository.save(asset);
        ra.addFlashAttribute("message", "Asset updated successfully.");
        return "redirect:/admin/assets";
    }

    // Delete asset
    @PostMapping("/assets/delete/{id}")
    @Transactional
    public String deleteAsset(@PathVariable Long id, RedirectAttributes ra) {
        if (!assetRepository.existsById(id)) {
            ra.addFlashAttribute("error", "Asset not found.");
            return "redirect:/admin/assets";
        }

        try {
            assetRepository.deleteById(id);
            ra.addFlashAttribute("message", "Asset deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "Cannot delete asset: It is referenced in vulnerabilities or incidents. Remove references first.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete asset. Please try again.");
        }

        return "redirect:/admin/assets";
    }
}