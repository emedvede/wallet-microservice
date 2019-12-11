package com.company.wallet.view.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TransactionModel {

    @NotBlank(message = "Field globalId is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field globalId is mandatory. It should be provided and can't be empty.")
    private String globalId;

    @NotBlank(message = "Field currency is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field currency is mandatory. It should be provided and can't be empty.")
    private String currency;

    @NotBlank(message = "Field walletId is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field walletId is mandatory. It should be provided and can't be empty.")
    private String walletId;

    @NotBlank(message = "Field transactionTypeId is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field transactionTypeId is mandatory. It should be provided and can't be empty.")
    private String transactionTypeId;

    @NotBlank(message = "Field amount is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field amount is mandatory. It should be provided and can't be empty.")
    private String amount;

    private String description;

    public TransactionModel(){}


    public TransactionModel( String globalId, String currency, String walletId, String transactionTypeId, String amount, String description) {
        this.globalId = globalId;
        this.currency = currency;
        this.walletId = walletId;
        this.transactionTypeId = transactionTypeId;
        this.amount = amount;
        this.description = description;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(String transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
