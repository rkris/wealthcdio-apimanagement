package com.wealthcdio.banking.service;

import com.wealthcdio.banking.dto.CreateAccountRequest;
import com.wealthcdio.banking.dto.TransferRequest;
import com.wealthcdio.banking.dto.UpdateAccountRequest;
import com.wealthcdio.banking.exception.BankingException;
import com.wealthcdio.banking.exception.ErrorCode;
import com.wealthcdio.banking.model.Account;
import com.wealthcdio.banking.model.Transaction;
import com.wealthcdio.banking.model.TransactionType;
import com.wealthcdio.banking.repository.AccountRepository;
import com.wealthcdio.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        LedgerService ledgerService = new LedgerService(transactionRepository);
        accountService = new AccountService(
                accountRepository,
                transactionRepository,
                new TransactionValidator(),
                ledgerService);
    }

    @Test
    void createAccount_persistsAccountWithInitialBalance() {
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.createAccount(CreateAccountRequest.builder()
                .accountHolder("Jane Doe")
                .initialBalance(new BigDecimal("500.00"))
                .build());

        assertThat(response.getAccountHolder()).isEqualTo("Jane Doe");
        assertThat(response.getBalance()).isEqualByComparingTo("500.00");

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("500.00");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_throwsWhenInsufficientFunds() {
        Account account = sampleAccount("acc-1", new BigDecimal("20.00"));
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw("acc-1", new BigDecimal("25.00"), null))
                .isInstanceOf(BankingException.class)
                .satisfies(ex -> assertThat(((BankingException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS));
    }

    @Test
    void transfer_updatesBothAccountsAndRecordsLedgerEntries() {
        Account from = sampleAccount("from", new BigDecimal("200.00"));
        Account to = sampleAccount("to", new BigDecimal("50.00"));

        when(accountRepository.findById("from")).thenReturn(Optional.of(from));
        when(accountRepository.findById("to")).thenReturn(Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var responses = accountService.transfer("from", TransferRequest.builder()
                .toAccountId("to")
                .amount(new BigDecimal("75.00"))
                .description("Rent")
                .build());

        assertThat(responses).hasSize(2);
        assertThat(from.getBalance()).isEqualByComparingTo("125.00");
        assertThat(to.getBalance()).isEqualByComparingTo("125.00");
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void getAccount_throwsWhenNotFound() {
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("missing"))
                .isInstanceOf(BankingException.class)
                .satisfies(ex -> assertThat(((BankingException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Test
    void updateAccount_changesHolderName() {
        Account account = sampleAccount("acc-1", new BigDecimal("0.00"));
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        var response = accountService.updateAccount("acc-1", UpdateAccountRequest.builder()
                .accountHolder("Updated Name")
                .build());

        assertThat(response.getAccountHolder()).isEqualTo("Updated Name");
    }

    @Test
    void getTransactionHistory_returnsLedgerOrderedByService() {
        Account account = sampleAccount("acc-1", new BigDecimal("10.00"));
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc("acc-1"))
                .thenReturn(List.of(sampleTransaction("t1", "acc-1", TransactionType.DEPOSIT)));

        var history = accountService.getTransactionHistory("acc-1");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
    }

    private Account sampleAccount(String id, BigDecimal balance) {
        Instant now = Instant.now();
        return Account.builder()
                .id(id)
                .accountHolder("Holder")
                .balance(balance)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Transaction sampleTransaction(String id, String accountId, TransactionType type) {
        return Transaction.builder()
                .id(id)
                .accountId(accountId)
                .transactionType(type)
                .amount(new BigDecimal("10.00"))
                .balanceAfter(new BigDecimal("10.00"))
                .createdAt(Instant.now())
                .build();
    }
}
