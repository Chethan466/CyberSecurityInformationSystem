package com.cybersecurity.cybersecurity_info_system.repository;

import com.cybersecurity.cybersecurity_info_system.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    boolean existsByAssetName(String assetName);
}