package com.fintech.btm.service;

import com.fintech.btm.dto.TransactionEvent;
import com.fintech.btm.model.Transaction;
import com.fintech.btm.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class TransactionConsumerService {

    private final TransactionRepository transactionRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public TransactionConsumerService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // @KafkaListener(topics = "transactions-stream", groupId = "btm-consumer-group")  // DISABLED FOR NOW
    public void consumeTransaction(TransactionEvent event) {
        try {
            log.info("Consumed transaction from Kafka: transactionId={}, userId={}, amount={}",
                    event.getTransactionId(), event.getUserId(), event.getAmount());

            Transaction transaction = new Transaction();
            transaction.setTransactionId(event.getTransactionId());
            transaction.setUserId(event.getUserId());
            transaction.setAmount(event.getAmount());
            transaction.setMerchantCategory(event.getMerchantCategory());
            transaction.setMerchantName(event.getMerchantName());
            transaction.setLocationLatitude(event.getLocationLatitude());
            transaction.setLocationLongitude(event.getLocationLongitude());

            try {
                LocalDateTime timestamp = LocalDateTime.parse(event.getTransactionTimestamp(), formatter);
                transaction.setTransactionTimestamp(timestamp);
            } catch (Exception e) {
                log.warn("Could not parse timestamp: {}, using current time", event.getTransactionTimestamp());
                transaction.setTransactionTimestamp(LocalDateTime.now());
            }

            transaction.setStatus("RECEIVED");

            transactionRepository.save(transaction);
            log.info("Transaction saved to database: transactionId={}", event.getTransactionId());

        } catch (Exception e) {
            log.error("Error processing transaction: {}", e.getMessage(), e);
        }
    }
}