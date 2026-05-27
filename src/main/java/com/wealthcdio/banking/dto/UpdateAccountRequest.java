package com.wealthcdio.banking.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateAccountRequest {

    @NotBlank(message = "accountHolder is required")
    private String accountHolder;

    public UpdateAccountRequest() {
    }

    public UpdateAccountRequest(String accountHolder) {
        this.accountHolder = accountHolder;
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

    public static final class Builder {
        private String accountHolder;

        public Builder accountHolder(String accountHolder) {
            this.accountHolder = accountHolder;
            return this;
        }

        public UpdateAccountRequest build() {
            return new UpdateAccountRequest(accountHolder);
        }
    }
}
