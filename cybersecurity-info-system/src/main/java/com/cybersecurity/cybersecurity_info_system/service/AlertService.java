package com.cybersecurity.cybersecurity_info_system.service;

import com.cybersecurity.cybersecurity_info_system.entity.*;
import com.cybersecurity.cybersecurity_info_system.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepo;

    @Transactional
    public void createVulnerabilityAlert(Long vulnId, String title, Vulnerability.Severity severity) {
        Alert alert = new Alert();
        alert.setAlertType(Alert.AlertType.VULNERABILITY);
        alert.setReferenceId(vulnId);
        alert.setDescription("New vulnerability reported: " + title);
        alert.setSeverity(mapVulnSeverity(severity));
        alertRepo.save(alert);
    }

    @Transactional
    public void createIncidentAlert(Long incidentId, String title, Incident.Severity severity) {
        Alert alert = new Alert();
        alert.setAlertType(Alert.AlertType.INCIDENT);
        alert.setReferenceId(incidentId);
        alert.setDescription("New incident reported: " + title);
        alert.setSeverity(mapIncidentSeverity(severity));
        alertRepo.save(alert);
    }

    // === MAPPING HELPERS ===
    private Alert.Severity mapVulnSeverity(Vulnerability.Severity s) {
        return switch (s) {
            case LOW -> Alert.Severity.LOW;
            case MEDIUM -> Alert.Severity.MEDIUM;
            case HIGH -> Alert.Severity.HIGH;
            case CRITICAL -> Alert.Severity.CRITICAL;
        };
    }

    private Alert.Severity mapIncidentSeverity(Incident.Severity s) {
        return switch (s) {
            case LOW -> Alert.Severity.LOW;
            case MEDIUM -> Alert.Severity.MEDIUM;
            case HIGH -> Alert.Severity.HIGH;
            case CRITICAL -> Alert.Severity.CRITICAL;
            case Unassigned -> Alert.Severity.LOW;
        };
    }
}