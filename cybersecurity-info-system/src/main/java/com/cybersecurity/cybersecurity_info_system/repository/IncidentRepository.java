package com.cybersecurity.cybersecurity_info_system.repository;

import com.cybersecurity.cybersecurity_info_system.entity.Incident;
import com.cybersecurity.cybersecurity_info_system.entity.Incident.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    long countByStatus(Status status);
}