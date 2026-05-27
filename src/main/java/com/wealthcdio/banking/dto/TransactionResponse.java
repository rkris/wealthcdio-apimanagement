package com.wealthcdio.banking.dto;

import com.wealthcdio.banking.model.Transaction;
import com.wealthcdio.banking.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {

    private String id;
    private String accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String counterpartyId;
    private String description;
    private Instant createdAt;

    public TransactionResponse() {
    }

    public static TransactionResponse from(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.id = transaction.getId();
        response.accountId = transaction.getAccountId();
        response.transactionType = transaction.getTransactionType();
        response.amount = transaction.getAmount();
        response.balanceAfter = transaction.getBalanceAfter();
        response.counterpartyId = transaction.getCounterpartyId();
        response.description = transaction.getDescription();
        response.createdAt = transaction.getCreatedAt();
        return response;
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getCounterpartyId() {
        return counterpartyId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
