package com.fintech.btm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "amount_mean")
    private BigDecimal amountMean;

    @Column(name = "amount_stddev")
    private BigDecimal amountStddev;

    @Column(name = "amount_p25")
    private BigDecimal amountP25;

    @Column(name = "amount_p50")
    private BigDecimal amountP50;

    @Column(name = "amount_p75")
    private BigDecimal amountP75;

    @Column(name = "amount_p90")
    private BigDecimal amountP90;

    @Column(name = "merchant_categories", columnDefinition = "text[]")
    private String[] merchantCategories;

    @Column(name = "locations", columnDefinition = "jsonb")
    private String locations; // Store JSON as string for now

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}