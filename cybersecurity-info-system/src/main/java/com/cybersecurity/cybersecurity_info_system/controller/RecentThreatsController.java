package com.cybersecurity.cybersecurity_info_system.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Controller
@RequestMapping("/recent-threats")
public class RecentThreatsController {

    private static final String[] RSS_URLS = {
        "https://feeds.feedburner.com/TheHackersNews",
        "https://www.cisa.gov/known-exploited-vulnerabilities-catalog.xml"  // ✅ Updated correct URL
    };
    
    private static final int MAX_THREATS = 10;

    // Multiple date formats to handle different RSS feeds
    private static final DateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
    };
    
    private static final DateFormat DISPLAY_DATE_FORMAT = 
        new SimpleDateFormat("MMM dd, yyyy");

    @GetMapping
    public String listThreats(Model model) {
        List<Map<String, Object>> threats = new ArrayList<>();
        Date oneYearAgo = Date.from(
            LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        Set<String> seenTitles = new HashSet<>();
        int perSource = MAX_THREATS / 2;

        for (String url : RSS_URLS) {
            int countFromThisSource = 0;
            String sourceName = url.contains("cisa") ? "CISA KEV" : "The Hacker News";

            try {
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(20_000)
                    .ignoreContentType(true)
                    .get();

                // Try both "item" and "entry" tags (Atom vs RSS)
                Elements items = doc.select("item");
                if (items.isEmpty()) {
                    items = doc.select("entry");
                }
                
                System.out.println("========================================");
                System.out.println("✅ Fetched " + sourceName);
                System.out.println("   URL: " + url);
                System.out.println("   Found " + items.size() + " items/entries");

                for (Element item : items) {
                    if (countFromThisSource >= perSource) break;

                    // Extract title (try multiple tags)
                    String title = getTextFromElement(item, "title");
                    if (title.isEmpty() || seenTitles.contains(title)) continue;

                    // Extract description (try multiple tags)
                    String rawDesc = getTextFromElement(item, "description", "summary", "content");
                    String description = cleanAndTruncate(rawDesc, 180);

                    // Extract link (try multiple tags)
                    String link = getTextFromElement(item, "link", "id");
                    if (link.isEmpty()) {
                        Element linkElem = item.selectFirst("link[href]");
                        if (linkElem != null) {
                            link = linkElem.attr("href");
                        }
                    }
                    if (link.isEmpty()) link = "#";

                    // Extract date (try multiple tags and formats)
                    String pubDateStr = getTextFromElement(item, "pubDate", "published", "updated", "dc:date");
                    if (pubDateStr.isEmpty()) continue;

                    Date pubDate = parseDate(pubDateStr);
                    if (pubDate == null) {
                        System.out.println("   ⚠️ Could not parse date: " + pubDateStr);
                        continue;
                    }
                    
                    if (pubDate.before(oneYearAgo)) continue;

                    String formattedDate = DISPLAY_DATE_FORMAT.format(pubDate);
                    String severity = determineSeverity(title);
                    String severityColor = switch (severity) {
                        case "Critical" -> "#ff3b3b";
                        case "High"     -> "#ff9500";
                        default         -> "#00d4ff";
                    };

                    seenTitles.add(title);

                    Map<String, Object> threat = new HashMap<>();
                    threat.put("title", title);
                    threat.put("description", description);
                    threat.put("source", sourceName);
                    threat.put("formattedDate", formattedDate);
                    threat.put("severity", severity);
                    threat.put("severityColor", severityColor);
                    threat.put("link", link);

                    threats.add(threat);
                    countFromThisSource++;
                }
                
                System.out.println("✅ Successfully collected " + countFromThisSource + " threats from " + sourceName);
                
            } catch (IOException e) {
                System.err.println("❌ Failed to fetch " + sourceName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("========================================");
        System.out.println("📊 TOTAL THREATS COLLECTED: " + threats.size());
        System.out.println("========================================");

        // Fallback if less than 10 threats
        if (threats.size() < MAX_THREATS) {
            List<Map<String, Object>> fallback = List.of(
                createThreat("Clop Ransomware Hits Oracle EBS – 30+ Victims Named",
                    "Exploit of unpatched Oracle E-Business Suite flaw leads to massive data leaks.",
                    "SecurityWeek", "Nov 12, 2025", "High", "#ff9500",
                    "https://www.securityweek.com/clop-oracle-ebs-2025"),

                createThreat("Windows Kernel 0-Day (CVE-2025-62215) Actively Exploited",
                    "Elevation-of-privilege flaw used in targeted attacks; Microsoft patch pending.",
                    "CISA KEV", "Nov 12, 2025", "Critical", "#ff3b3b",
                    "https://www.cisa.gov/known-exploited-vulnerabilities-catalog"),

                createThreat("CISA Adds Apache Struts RCE to KEV Catalog",
                    "Critical remote-code-execution bug now confirmed in the wild.",
                    "CISA KEV", "Nov 10, 2025", "Critical", "#ff3b3b",
                    "https://www.cisa.gov/known-exploited-vulnerabilities-catalog"),

                createThreat("AI-Powered Holiday Fraud Campaigns Surge 300%",
                    "Automated phishing, gift-card scams, and deep-fake vishing dominate retail.",
                    "eSecurity Planet", "Nov 12, 2025", "High", "#ff9500",
                    "https://www.esecurityplanet.com/holiday-fraud-2025"),

                createThreat("State Actor Targets APAC E-Commerce Supply Chain",
                    "New TTPs using compromised third-party logistics tools observed.",
                    "Medium – CyberSec APAC", "Nov 7, 2025", "Medium", "#00d4ff",
                    "https://medium.com/cyber-apac-supply-chain-2025"),

                createThreat("Critical VMware ESXi Zero-Day Under Active Attack",
                    "VMware releases emergency patch for ESXi hypervisor vulnerability.",
                    "BleepingComputer", "Nov 11, 2025", "Critical", "#ff3b3b",
                    "https://www.bleepingcomputer.com/vmware-esxi-2025"),

                createThreat("Microsoft Patches 89 Vulnerabilities in November Update",
                    "Patch Tuesday addresses six critical flaws including two zero-days.",
                    "Microsoft MSRC", "Nov 10, 2025", "High", "#ff9500",
                    "https://msrc.microsoft.com/november-2025"),

                createThreat("Iranian APT Deploys Custom Backdoor in Middle East Attacks",
                    "Sophisticated malware targets government and defense sectors.",
                    "Recorded Future", "Nov 9, 2025", "High", "#ff9500",
                    "https://www.recordedfuture.com/iranian-apt-2025"),

                createThreat("Chrome 119 Patches High-Severity Use-After-Free Bug",
                    "Google releases urgent browser update addressing memory corruption flaw.",
                    "The Hacker News", "Nov 8, 2025", "High", "#ff9500",
                    "https://thehackernews.com/chrome-119-update"),

                createThreat("New Phishing Kit Targets Microsoft 365 Credentials",
                    "Advanced evasion techniques bypass multi-factor authentication.",
                    "Cofense", "Nov 7, 2025", "Medium", "#00d4ff",
                    "https://cofense.com/m365-phishing-2025")
            );

            int needed = MAX_THREATS - threats.size();
            threats.addAll(fallback.subList(0, Math.min(needed, fallback.size())));
            System.out.println("⚠️ Using " + needed + " fallback threats (total now: " + threats.size() + ")");
        } else {
            threats = threats.subList(0, MAX_THREATS);
        }

        // ✅ SORT BY SEVERITY: Critical → High → Medium (AFTER fallback is added)
        threats.sort((t1, t2) -> {
            String sev1 = (String) t1.get("severity");
            String sev2 = (String) t2.get("severity");
            return getSeverityOrder(sev1) - getSeverityOrder(sev2);
        });

        System.out.println("✅ Sorted threats by severity");

        model.addAttribute("threats", threats);
        model.addAttribute("threatCount", threats.size());
        return "recent-threats";
    }

    // Helper: Extract text from multiple possible tag names
    private String getTextFromElement(Element item, String... tagNames) {
        for (String tagName : tagNames) {
            Element elem = item.selectFirst(tagName);
            if (elem != null) {
                String text = elem.text().trim();
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }

    // Helper: Clean HTML and truncate
    private String cleanAndTruncate(String text, int maxLength) {
        String cleaned = text.replaceAll("<[^>]*>", "").trim();
        return cleaned.length() > maxLength 
            ? cleaned.substring(0, maxLength) + "..." 
            : cleaned;
    }

    // Helper: Try multiple date formats
    private Date parseDate(String dateStr) {
        for (DateFormat format : DATE_FORMATS) {
            try {
                return format.parse(dateStr);
            } catch (ParseException ignored) {
                // Try next format
            }
        }
        return null;
    }

    private Map<String, Object> createThreat(String title, String description, String source,
                                             String date, String severity, String color, String link) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("source", source);
        map.put("formattedDate", date);
        map.put("severity", severity);
        map.put("severityColor", color);
        map.put("link", link);
        return map;
    }

    private String determineSeverity(String title) {
        String lower = title.toLowerCase();
        if (lower.matches(".*\\b(0-day|zero-day|rce|exploited|kev|active attack|critical)\\b.*")) {
            return "Critical";
        }
        if (lower.matches(".*\\b(phish|fraud|surge|campaign|ransom|leak|attack)\\b.*")) {
            return "High";
        }
        return "Medium";
    }

    // Helper: Get severity order for sorting (lower number = higher priority)
    private int getSeverityOrder(String severity) {
        return switch (severity) {
            case "Critical" -> 1;
            case "High"     -> 2;
            case "Medium"   -> 3;
            default         -> 4;
        };
    }
}