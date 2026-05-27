package com.wealthcdio.banking.service;

import com.wealthcdio.banking.exception.BankingException;
import com.wealthcdio.banking.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionValidatorTest {

    private TransactionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
    }

    @Test
    void validatePositiveAmount_acceptsPositiveValue() {
        validator.validatePositiveAmount(new BigDecimal("10.50"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1", "-0.01"})
    void validatePositiveAmount_rejectsNonPositive(String amount) {
        assertThatThrownBy(() -> validator.validatePositiveAmount(new BigDecimal(amount)))
                .isInstanceOf(BankingException.class)
                .satisfies(ex -> assertThat(((BankingException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_AMOUNT));
    }

    @Test
    void validatePositiveAmount_rejectsNull() {
        assertThatThrownBy(() -> validator.validatePositiveAmount(null))
                .isInstanceOf(BankingException.class);
    }

    @Test
    void validateSufficientFunds_acceptsWhenBalanceCoversAmount() {
        validator.validateSufficientFunds(new BigDecimal("100.00"), new BigDecimal("50.00"));
    }

    @Test
    void validateSufficientFunds_rejectsOverdraft() {
        assertThatThrownBy(() ->
                validator.validateSufficientFunds(new BigDecimal("40.00"), new BigDecimal("40.01")))
                .isInstanceOf(BankingException.class)
                .satisfies(ex -> assertThat(((BankingException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS));
    }

    @Test
    void validateTransferAccounts_rejectsSameAccount() {
        assertThatThrownBy(() -> validator.validateTransferAccounts("acc-1", "acc-1"))
                .isInstanceOf(BankingException.class)
                .satisfies(ex -> assertThat(((BankingException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SAME_ACCOUNT_TRANSFER));
    }
}
