package com.fintech.btm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    @JsonProperty("transactionId")
    private Long transactionId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("merchantCategory")
    private String merchantCategory;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("locationLatitude")
    private Double locationLatitude;

    @JsonProperty("locationLongitude")
    private Double locationLongitude;

    @JsonProperty("transactionTimestamp")
    private String transactionTimestamp;  // Changed to String
}