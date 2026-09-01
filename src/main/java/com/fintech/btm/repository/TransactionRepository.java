package com.fintech.btm.repository;

import com.fintech.btm.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndTransactionTimestampBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}