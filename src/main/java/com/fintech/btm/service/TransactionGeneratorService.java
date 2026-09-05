package com.fintech.btm.service;

import com.fintech.btm.model.Transaction;
import com.fintech.btm.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class TransactionGeneratorService {

    private final TransactionRepository transactionRepository;
    private final Random random = new Random();

    // Merchant categories and their typical amount ranges
    private static final String[] MERCHANT_CATEGORIES = {
            "GROCERY", "RESTAURANT", "GAS", "ENTERTAINMENT", "TRAVEL",
            "SHOPPING", "UTILITIES", "HEALTHCARE", "TRANSPORT", "ONLINE"
    };

    private static final String[] MERCHANTS = {
            "Carrefour Supermarket", "Nairobi Restaurant", "Shell Petrol",
            "Cineplex Cinema", "Kenya Airways", "Nakumatt Mall", "Safaricom Bill",
            "Aga Khan Hospital", "Uber Kenya", "Amazon Kenya"
    };

    // Nairobi coordinates (approximate bounds)
    private static final double LAT_MIN = -1.35;
    private static final double LAT_MAX = -1.20;
    private static final double LON_MIN = 36.70;
    private static final double LON_MAX = 36.95;

    public TransactionGeneratorService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void generateAndSaveTransaction(Long userId) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);

        // Randomly select merchant category
        String category = MERCHANT_CATEGORIES[random.nextInt(MERCHANT_CATEGORIES.length)];
        transaction.setMerchantCategory(category);
        transaction.setMerchantName(MERCHANTS[random.nextInt(MERCHANTS.length)]);

        // Generate realistic amount based on category
        BigDecimal amount = generateAmountByCategory(category);
        transaction.setAmount(amount);

        // Random location in Nairobi
        transaction.setLocationLatitude(LAT_MIN + (LAT_MAX - LAT_MIN) * random.nextDouble());
        transaction.setLocationLongitude(LON_MIN + (LON_MAX - LON_MIN) * random.nextDouble());

        // Random timestamp in last 30 days
        transaction.setTransactionTimestamp(LocalDateTime.now().minusDays(random.nextInt(30)));

        transaction.setStatus("COMPLETED");

        transactionRepository.save(transaction);
    }

    private BigDecimal generateAmountByCategory(String category) {
        double amount;

        switch (category) {
            case "GROCERY":
                amount = 500 + random.nextDouble() * 3000;  // 500-3500
                break;
            case "RESTAURANT":
                amount = 800 + random.nextDouble() * 4000;  // 800-4800
                break;
            case "GAS":
                amount = 1500 + random.nextDouble() * 3500; // 1500-5000
                break;
            case "ENTERTAINMENT":
                amount = 1000 + random.nextDouble() * 4000; // 1000-5000
                break;
            case "TRAVEL":
                amount = 5000 + random.nextDouble() * 20000; // 5000-25000
                break;
            case "SHOPPING":
                amount = 2000 + random.nextDouble() * 10000; // 2000-12000
                break;
            case "UTILITIES":
                amount = 500 + random.nextDouble() * 2000;  // 500-2500
                break;
            case "HEALTHCARE":
                amount = 1000 + random.nextDouble() * 8000; // 1000-9000
                break;
            case "TRANSPORT":
                amount = 100 + random.nextDouble() * 500;   // 100-600
                break;
            case "ONLINE":
                amount = 500 + random.nextDouble() * 5000;  // 500-5500
                break;
            default:
                amount = 1000 + random.nextDouble() * 5000; // 1000-6000
        }

        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void generateBulkTransactions(Long userId, int count) {
        log.info("Generating {} transactions for userId={}", count, userId);
        for (int i = 0; i < count; i++) {
            generateAndSaveTransaction(userId);
        }
        log.info("Completed generating {} transactions", count);
    }
}