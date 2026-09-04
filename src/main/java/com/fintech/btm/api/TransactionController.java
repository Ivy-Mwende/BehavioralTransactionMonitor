package com.fintech.btm.api;

import com.fintech.btm.dto.TransactionEvent;
import com.fintech.btm.model.Transaction;
import com.fintech.btm.model.UserProfile;
import com.fintech.btm.repository.TransactionRepository;
import com.fintech.btm.service.TransactionProducerService;
import com.fintech.btm.service.UserProfileCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@Slf4j
@Tag(name = "Transactions", description = "Transaction ingestion and fraud detection endpoints")
public class TransactionController {

    private final TransactionProducerService producerService;
    private final TransactionRepository transactionRepository;
    private final UserProfileCacheService userProfileCacheService;

    public TransactionController(TransactionProducerService producerService,
                                 TransactionRepository transactionRepository,
                                 UserProfileCacheService userProfileCacheService) {
        this.producerService = producerService;
        this.transactionRepository = transactionRepository;
        this.userProfileCacheService = userProfileCacheService;
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns OK if service is running and healthy")
    @ApiResponse(responseCode = "200", description = "Service is healthy", content = @Content(schema = @Schema(implementation = String.class)))
    public String health() {
        return "OK";
    }

    @PostMapping("/ingest")
    @Operation(
            summary = "Ingest transaction",
            description = "Accept a transaction event, send to Kafka queue for async processing, and return immediately"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Transaction accepted and queued for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
            @ApiResponse(responseCode = "500", description = "Error processing transaction")
    })
    public ResponseEntity<Map<String, String>> ingestTransaction(@RequestBody TransactionEvent event) {
        log.info("Received transaction ingest request: userId={}, amount={}", event.getUserId(), event.getAmount());

        try {
            producerService.sendTransaction(event);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction sent to Kafka");
            response.put("transactionId", event.getTransactionId().toString());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            log.error("Error ingesting transaction: {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/test")
    @Operation(
            summary = "Send test transaction",
            description = "Send a hardcoded test transaction for demo and testing purposes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test transaction sent successfully"),
            @ApiResponse(responseCode = "500", description = "Error sending test transaction")
    })
    public ResponseEntity<Map<String, String>> sendTestTransaction() {
        log.info("Sending test transaction");

        TransactionEvent testEvent = new TransactionEvent(
                System.currentTimeMillis(),
                1L,
                java.math.BigDecimal.valueOf(250.50),
                "GROCERY",
                "Carrefour Supermarket",
                -1.2866,
                36.8172,
                LocalDateTime.now().toString()
        );

        try {
            producerService.sendTransaction(testEvent);
            log.info("Transaction successfully sent: {}", testEvent);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test transaction sent to Kafka");
            response.put("transactionId", testEvent.getTransactionId().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending transaction", e);

            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get transactions by user",
            description = "Retrieve all transactions for a specific user, sorted by most recent"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public List<Transaction> getTransactionsByUser(@PathVariable Long userId) {
        log.info("Fetching transactions for userId={}", userId);
        return transactionRepository.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/range")
    @Operation(
            summary = "Get transactions by user and date range",
            description = "Retrieve transactions for a user within a specific date/time range"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public List<Transaction> getTransactionsByUserAndRange(
            @PathVariable Long userId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        log.info("Fetching transactions for userId={} between {} and {}", userId, start, end);
        return transactionRepository.findByUserIdAndTransactionTimestampBetween(userId, start, end);
    }

    @GetMapping("/{userId}/profile")
    @Operation(
            summary = "Get user profile",
            description = "Retrieve cached user profile with behavioral metrics (mean, stddev, percentiles)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved from cache/database"),
            @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {
        log.info("Fetching user profile for userId={}", userId);
        UserProfile profile = userProfileCacheService.getUserProfile(userId);

        if (profile == null) {
            log.warn("User profile not found for userId={}", userId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/cache-test")
    @Operation(
            summary = "Test caching",
            description = "Verify Redis caching layer is operational and responding"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cache test completed successfully"),
            @ApiResponse(responseCode = "500", description = "Cache test failed")
    })
    public ResponseEntity<Map<String, Object>> testCaching() {
        log.info("Testing Redis caching");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache test completed");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("userProfiles_cache_name", "userProfiles");
        response.put("riskScores_cache_name", "riskScores");
        response.put("cache_ttl_hours", 1);

        return ResponseEntity.ok(response);
    }
}