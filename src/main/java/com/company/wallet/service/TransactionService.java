package com.company.wallet.service;

import com.company.wallet.entities.Transaction;
import com.company.wallet.exceptions.WalletException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for managing transactions
 * @author Elena Medvedeva
 */
public interface TransactionService {
    public List<Transaction> getTransactionsByWalletId(@NotNull Integer walletId) throws WalletException;
    public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currencyName, @NotBlank String walletId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws WalletException;

}
