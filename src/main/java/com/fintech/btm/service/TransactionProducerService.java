package com.fintech.btm.service;

import com.fintech.btm.dto.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionProducerService {

    private static final String TOPIC = "transactions-stream";
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionProducerService(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(TransactionEvent event) {
        log.info("Sending transaction to Kafka: transactionId={}, userId={}, amount={}",
                event.getTransactionId(), event.getUserId(), event.getAmount());

        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);
    }
}
