package com.company.wallet.view.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class WalletModel {

    @NotBlank(message = "Field userId is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field userId is mandatory. It should be provided and can't be empty.")
    private String userId;

    @NotBlank(message = "Field currency is mandatory. It should be provided and can't be empty.")
    @NotNull(message = "Field currency is mandatory. It should be provided and can't be empty.")
    private String currency;

    public WalletModel(){}

    public WalletModel(String userId, String currency) {
        this.userId = userId;
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
