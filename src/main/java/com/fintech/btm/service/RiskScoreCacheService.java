package com.fintech.btm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RiskScoreCacheService {

    // Cache risk scores by transactionId
    @Cacheable(value = "riskScores", key = "#transactionId", unless = "#result == null")
    public Map<String, Object> getRiskScore(Long transactionId) {
        log.info("Computing risk score for transaction: transactionId={}", transactionId);
        // Placeholder - will be implemented in Phase 2 (behavioral detection)
        return null;
    }

    // Cache risk scores by userId
    @Cacheable(value = "userRiskScores", key = "#userId", unless = "#result == null")
    public Double getUserRiskScore(Long userId) {
        log.info("Fetching user risk score from cache: userId={}", userId);
        // Placeholder - will be implemented in Phase 2
        return null;
    }

    @CacheEvict(value = "riskScores", key = "#transactionId")
    public void evictRiskScore(Long transactionId) {
        log.info("Evicting risk score cache: transactionId={}", transactionId);
    }

    @CacheEvict(value = "userRiskScores", key = "#userId")
    public void evictUserRiskScore(Long userId) {
        log.info("Evicting user risk score cache: userId={}", userId);
    }

    @CacheEvict(value = {"riskScores", "userRiskScores"}, allEntries = true)
    public void evictAllCaches() {
        log.info("Evicting all risk score caches");
    }
}
