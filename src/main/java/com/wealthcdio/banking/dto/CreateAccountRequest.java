package com.wealthcdio.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAccountRequest {

    @NotBlank(message = "accountHolder is required")
    private String accountHolder;

    @NotNull(message = "initialBalance is required")
    @DecimalMin(value = "0.00", message = "initialBalance must be zero or positive")
    private BigDecimal initialBalance;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(String accountHolder, BigDecimal initialBalance) {
        this.accountHolder = accountHolder;
        this.initialBalance = initialBalance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public static final class Builder {
        private String accountHolder;
        private BigDecimal initialBalance;

        public Builder accountHolder(String accountHolder) {
            this.accountHolder = accountHolder;
            return this;
        }

        public Builder initialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public CreateAccountRequest build() {
            return new CreateAccountRequest(accountHolder, initialBalance);
        }
    }
}
