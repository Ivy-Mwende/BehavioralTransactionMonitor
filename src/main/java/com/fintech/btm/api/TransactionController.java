package com.fintech.btm.api;

import com.fintech.btm.dto.TransactionEvent;
import com.fintech.btm.model.Transaction;
import com.fintech.btm.repository.TransactionRepository;
import com.fintech.btm.service.TransactionProducerService;
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
public class TransactionController {

    private final TransactionProducerService producerService;
    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionProducerService producerService,
                                 TransactionRepository transactionRepository) {
        this.producerService = producerService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping("/ingest")
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
    public List<Transaction> getTransactionsByUser(@PathVariable Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/range")
    public List<Transaction> getTransactionsByUserAndRange(
            @PathVariable Long userId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return transactionRepository.findByUserIdAndTransactionTimestampBetween(userId, start, end);
    }
}