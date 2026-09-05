package com.fintech.btm.service;

import com.fintech.btm.model.User;
import com.fintech.btm.repository.UserRepository;
import com.fintech.btm.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class DataSeederService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionGeneratorService transactionGeneratorService;
    private static final int NUM_USERS = 100;
    private static final int TRANSACTIONS_PER_USER = 50;

    public DataSeederService(UserRepository userRepository,
                             TransactionRepository transactionRepository,
                             TransactionGeneratorService transactionGeneratorService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transactionGeneratorService = transactionGeneratorService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDataOnStartup() {
        log.info("Starting data seeding on application startup");

        // Only skip if TRANSACTIONS already exist
        if (transactionRepository.count() > 0) {
            log.info("Transactions already exist. Skipping seeding.");
            return;
        }

        try {
            // Only seed users if they don't exist
            if (userRepository.count() == 0) {
                seedUsers();
            }

            seedTransactions();
            log.info("Data seeding completed successfully");
        } catch (Exception e) {
            log.error("Error seeding data: {}", e.getMessage(), e);
        }
    }

    private void seedUsers() {
        log.info("Seeding {} synthetic users", NUM_USERS);
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= NUM_USERS; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setOnboardDate(LocalDateTime.now().minusDays(new Random().nextInt(365)));
            user.setLastActivityDate(LocalDateTime.now().minusDays(new Random().nextInt(30)));
            users.add(user);
        }

        userRepository.saveAll(users);
        log.info("Successfully seeded {} users", NUM_USERS);
    }

    private void seedTransactions() {
        log.info("Seeding transactions ({} per user)", TRANSACTIONS_PER_USER);

        List<User> allUsers = userRepository.findAll();
        long totalTransactions = 0;

        for (User user : allUsers) {
            for (int i = 0; i < TRANSACTIONS_PER_USER; i++) {
                transactionGeneratorService.generateAndSaveTransaction(user.getUserId());
                totalTransactions++;
            }
        }

        log.info("Successfully seeded {} total transactions", totalTransactions);
    }
}