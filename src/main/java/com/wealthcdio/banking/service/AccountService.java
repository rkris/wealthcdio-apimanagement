package com.wealthcdio.banking.service;

import com.wealthcdio.banking.dto.AccountResponse;
import com.wealthcdio.banking.dto.CreateAccountRequest;
import com.wealthcdio.banking.dto.TransactionResponse;
import com.wealthcdio.banking.dto.TransferRequest;
import com.wealthcdio.banking.dto.UpdateAccountRequest;
import com.wealthcdio.banking.exception.BankingException;
import com.wealthcdio.banking.exception.ErrorCode;
import com.wealthcdio.banking.model.Account;
import com.wealthcdio.banking.model.Transaction;
import com.wealthcdio.banking.model.TransactionType;
import com.wealthcdio.banking.repository.AccountRepository;
import com.wealthcdio.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionValidator transactionValidator;
    private final LedgerService ledgerService;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransactionValidator transactionValidator,
            LedgerService ledgerService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionValidator = transactionValidator;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        BigDecimal initialBalance = request.getInitialBalance();
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankingException(ErrorCode.INVALID_AMOUNT, "initialBalance must be zero or positive");
        }

        Instant now = Instant.now();
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .accountHolder(request.getAccountHolder())
                .balance(initialBalance)
                .createdAt(now)
                .updatedAt(now)
                .build();

        account = accountRepository.save(account);

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.record(
                    account.getId(),
                    TransactionType.DEPOSIT,
                    initialBalance,
                    account.getBalance(),
                    null,
                    "Initial deposit");
        }

        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountId) {
        return AccountResponse.from(findAccountOrThrow(accountId));
    }

    @Transactional
    public AccountResponse updateAccount(String accountId, UpdateAccountRequest request) {
        Account account = findAccountOrThrow(accountId);
        account.setAccountHolder(request.getAccountHolder());
        account.setUpdatedAt(Instant.now());
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public TransactionResponse deposit(String accountId, BigDecimal amount, String description) {
        transactionValidator.validatePositiveAmount(amount);
        Account account = findAccountOrThrow(accountId);

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        Transaction transaction = ledgerService.record(
                accountId,
                TransactionType.DEPOSIT,
                amount,
                account.getBalance(),
                null,
                description);

        return TransactionResponse.from(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(String accountId, BigDecimal amount, String description) {
        transactionValidator.validatePositiveAmount(amount);
        Account account = findAccountOrThrow(accountId);
        transactionValidator.validateSufficientFunds(account.getBalance(), amount);

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        Transaction transaction = ledgerService.record(
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                account.getBalance(),
                null,
                description);

        return TransactionResponse.from(transaction);
    }

    @Transactional
    public List<TransactionResponse> transfer(String fromAccountId, TransferRequest request) {
        transactionValidator.validatePositiveAmount(request.getAmount());
        transactionValidator.validateTransferAccounts(fromAccountId, request.getToAccountId());

        Account fromAccount = findAccountOrThrow(fromAccountId);
        Account toAccount = findAccountOrThrow(request.getToAccountId());
        transactionValidator.validateSufficientFunds(fromAccount.getBalance(), request.getAmount());

        BigDecimal amount = request.getAmount();
        Instant now = Instant.now();

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        fromAccount.setUpdatedAt(now);
        toAccount.setBalance(toAccount.getBalance().add(amount));
        toAccount.setUpdatedAt(now);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction debit = ledgerService.record(
                fromAccountId,
                TransactionType.TRANSFER_OUT,
                amount,
                fromAccount.getBalance(),
                request.getToAccountId(),
                request.getDescription());

        Transaction credit = ledgerService.record(
                request.getToAccountId(),
                TransactionType.TRANSFER_IN,
                amount,
                toAccount.getBalance(),
                fromAccountId,
                request.getDescription());

        return List.of(TransactionResponse.from(debit), TransactionResponse.from(credit));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String accountId) {
        findAccountOrThrow(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    private Account findAccountOrThrow(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BankingException(
                        ErrorCode.ACCOUNT_NOT_FOUND,
                        "Account not found: " + accountId));
    }
}
