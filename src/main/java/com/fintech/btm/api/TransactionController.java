package com.fintech.btm.api;

import com.fintech.btm.dto.TransactionEvent;
import com.fintech.btm.model.Transaction;
import com.fintech.btm.model.UserProfile;
import com.fintech.btm.repository.TransactionRepository;
import com.fintech.btm.repository.UserRepository;
import com.fintech.btm.service.TransactionProducerService;
import com.fintech.btm.service.UserProfileCacheService;
import com.fintech.btm.service.DataSeederService;
import com.fintech.btm.service.TransactionGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transactions", description = "Transaction endpoints")
public class TransactionController {

    private final TransactionProducerService producerService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserProfileCacheService userProfileCacheService;
    private final DataSeederService dataSeederService;
    private final TransactionGeneratorService transactionGeneratorService;

    public TransactionController(TransactionProducerService producerService,
                                 TransactionRepository transactionRepository,
                                 UserRepository userRepository,
                                 UserProfileCacheService userProfileCacheService,
                                 DataSeederService dataSeederService,
                                 TransactionGeneratorService transactionGeneratorService) {
        this.producerService = producerService;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userProfileCacheService = userProfileCacheService;
        this.dataSeederService = dataSeederService;
        this.transactionGeneratorService = transactionGeneratorService;
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public String health() {
        return "OK";
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest transaction")
    public ResponseEntity<Map<String, String>> ingestTransaction(@RequestBody TransactionEvent event) {
        log.info("Received transaction: userId={}, amount={}", event.getUserId(), event.getAmount());
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Transaction sent to Kafka");
        response.put("transactionId", event.getTransactionId().toString());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/test")
    @Operation(summary = "Send test transaction")
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

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Test transaction sent");
        response.put("transactionId", testEvent.getTransactionId().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transactions by user")
    public List<Transaction> getTransactionsByUser(@PathVariable Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/range")
    @Operation(summary = "Get transactions by date range")
    public List<Transaction> getTransactionsByUserAndRange(
            @PathVariable Long userId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return transactionRepository.findByUserIdAndTransactionTimestampBetween(userId, start, end);
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long userId) {
        UserProfile profile = userProfileCacheService.getUserProfile(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/cache-test")
    @Operation(summary = "Test caching")
    public ResponseEntity<Map<String, Object>> testCaching() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache test completed");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("cache_ttl_hours", 1);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed database")
    public ResponseEntity<Map<String, String>> seedDatabase() {
        dataSeederService.seedDataOnStartup();
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Database seeding completed");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/generate-transactions")
    @Operation(summary = "Generate transactions")
    public ResponseEntity<Map<String, String>> generateTransactionsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int count) {
        transactionGeneratorService.generateBulkTransactions(userId, count);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Generated " + count + " transactions");
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get database statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        long userCount = userRepository.count();
        long transactionCount = transactionRepository.count();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_users", userCount);
        stats.put("total_transactions", transactionCount);
        stats.put("avg_transactions_per_user", userCount > 0 ? transactionCount / userCount : 0);
        stats.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(stats);
    }
}