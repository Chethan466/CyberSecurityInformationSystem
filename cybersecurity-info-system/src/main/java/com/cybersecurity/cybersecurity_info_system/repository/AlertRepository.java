package com.cybersecurity.cybersecurity_info_system.repository;

import com.cybersecurity.cybersecurity_info_system.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findTop20ByOrderByCreatedAtDesc();
    long countByStatus(Alert.Status status);
}