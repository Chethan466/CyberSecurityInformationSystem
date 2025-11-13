package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.entity.User;
import com.cybersecurity.cybersecurity_info_system.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);
    private static final String RSS_URL = "https://feeds.feedburner.com/TheHackersNews";

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();

        // ---- USER & ROLE ----
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("username", username);
        model.addAttribute("role", user.getRole().name());

        // ---- THREAT COUNT ----
        int threatCount = fetchRecentThreatCount();
        model.addAttribute("threatCount", threatCount);

        return "dashboard";
    }

    /**
     * Returns the number of recent threats (max 5) from The Hacker News RSS.
     * On any failure it safely returns 5 (fallback) so the UI never shows 0.
     */
    private int fetchRecentThreatCount() {
        try {
            Document doc = Jsoup.connect(RSS_URL)
                    .userAgent("Mozilla/5.0 (CyberShield Dashboard)")
                    .timeout(10_000)          // 10 s
                    .get();

            Elements items = doc.select("item");
            int size = items.size();
            LOG.debug("Fetched {} items from RSS → using {}", size, Math.min(size, 5));
            return Math.min(size, 5);

        } catch (IOException e) {
            LOG.warn("RSS fetch failed (using fallback count = 5): {}", e.getMessage());
            return 5;   // always show something
        }
    }
}