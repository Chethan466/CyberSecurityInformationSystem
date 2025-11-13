package com.cybersecurity.cybersecurity_info_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showIndexPage() {
        return "index"; // → src/main/resources/templates/index.html
    }
}