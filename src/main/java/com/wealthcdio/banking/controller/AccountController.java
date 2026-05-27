package com.wealthcdio.banking.controller;

import com.wealthcdio.banking.dto.AccountResponse;
import com.wealthcdio.banking.dto.AmountRequest;
import com.wealthcdio.banking.dto.CreateAccountRequest;
import com.wealthcdio.banking.dto.TransactionResponse;
import com.wealthcdio.banking.dto.TransferRequest;
import com.wealthcdio.banking.dto.UpdateAccountRequest;
import com.wealthcdio.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Banking account and transaction operations")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account", description = "Creates a new account with an optional initial balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account", description = "Returns account details and current balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public AccountResponse getAccount(
            @Parameter(description = "Account UUID") @PathVariable String accountId) {
        return accountService.getAccount(accountId);
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update account", description = "Updates the account holder name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public AccountResponse updateAccount(
            @Parameter(description = "Account UUID") @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateAccount(accountId, request);
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Deposit", description = "Deposits funds into the account and records a ledger entry.")
    public TransactionResponse deposit(
            @PathVariable String accountId,
            @Valid @RequestBody AmountRequest request) {
        return accountService.deposit(accountId, request.getAmount(), request.getDescription());
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Withdraw", description = "Withdraws funds if sufficient balance is available.")
    public TransactionResponse withdraw(
            @PathVariable String accountId,
            @Valid @RequestBody AmountRequest request) {
        return accountService.withdraw(accountId, request.getAmount(), request.getDescription());
    }

    @PostMapping("/{accountId}/transfer")
    @Operation(summary = "Transfer", description = "Transfers funds to another account. Returns debit and credit ledger entries.")
    public List<TransactionResponse> transfer(
            @PathVariable String accountId,
            @Valid @RequestBody TransferRequest request) {
        return accountService.transfer(accountId, request);
    }

    @GetMapping("/{accountId}/transactions")
    @Operation(summary = "Transaction history", description = "Lists all transactions for the account, newest first.")
    public List<TransactionResponse> getTransactions(@PathVariable String accountId) {
        return accountService.getTransactionHistory(accountId);
    }
}
