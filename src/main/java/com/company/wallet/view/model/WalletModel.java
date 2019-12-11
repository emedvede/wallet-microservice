package com.company.wallet.view.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.company.wallet.exceptions.ErrorMessage.PART_NO_MANDATORY_FIELD;

public class WalletModel {

    @NotBlank(message = "Field userId" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field userId" + PART_NO_MANDATORY_FIELD)
    private String userId;

    @NotBlank(message = "Field currency" + PART_NO_MANDATORY_FIELD)
    @NotNull(message = "Field currency" + PART_NO_MANDATORY_FIELD)
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
