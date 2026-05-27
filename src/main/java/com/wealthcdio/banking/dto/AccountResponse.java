package com.wealthcdio.banking.dto;

import com.wealthcdio.banking.model.Account;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountResponse {

    private String id;
    private String accountHolder;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;

    public AccountResponse() {
    }

    public AccountResponse(String id, String accountHolder, BigDecimal balance, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountHolder(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
