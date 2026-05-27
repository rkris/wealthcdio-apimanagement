package com.wealthcdio.banking.service;

import com.wealthcdio.banking.model.Transaction;
import com.wealthcdio.banking.model.TransactionType;
import com.wealthcdio.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class LedgerService {

    private final TransactionRepository transactionRepository;

    public LedgerService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction record(
            String accountId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String counterpartyId,
            String description) {

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .accountId(accountId)
                .transactionType(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .counterpartyId(counterpartyId)
                .description(description)
                .createdAt(Instant.now())
                .build();

        return transactionRepository.save(transaction);
    }
}
