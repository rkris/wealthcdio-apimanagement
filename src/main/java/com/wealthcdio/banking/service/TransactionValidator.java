package com.wealthcdio.banking.service;

import com.wealthcdio.banking.exception.BankingException;
import com.wealthcdio.banking.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionValidator {

    public void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException(ErrorCode.INVALID_AMOUNT, "Amount must be greater than zero");
        }
    }

    public void validateSufficientFunds(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new BankingException(ErrorCode.INSUFFICIENT_FUNDS, "Insufficient funds for this operation");
        }
    }

    public void validateTransferAccounts(String fromAccountId, String toAccountId) {
        if (fromAccountId.equals(toAccountId)) {
            throw new BankingException(ErrorCode.SAME_ACCOUNT_TRANSFER, "Cannot transfer to the same account");
        }
    }
}
