// File: src/main/java/com/yourcompany/cybershield/controller/StatsController.java

/*package com.cybersecurity.cybersecurity_info_system.controller;

import com.cybersecurity.cybersecurity_info_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Stats> streamStats() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(sequence -> new Stats(
                        assetRepository.count(),
                        incidentRepository.countByStatus("OPEN"),
                        vulnerabilityRepository.countBySeverity("CRITICAL"),
                        alertRepository.countByReadFalse()
                ));
    }

    // Inner record for JSON serialization
    record Stats(
            long assets,
            long openIncidents,
            long criticalVulns,
            long unreadAlerts
    ) {}
}*/