package com.company.wallet.service;

import com.company.wallet.entities.Wallet;
import com.company.wallet.exceptions.WalletException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for managing wallets
 * @author Elena Medvedeva
 */
public interface WalletService {
    public List<Wallet> findAll() throws WalletException;
    public Wallet findById(@NotNull Integer id) throws WalletException;
    public List<Wallet> findByUserId(@NotBlank String userId) throws WalletException;
    public Wallet createWallet(@NotBlank String userId, @NotBlank String currencyName) throws WalletException;
    public Wallet updateWalletAmount(@NotNull Wallet wallet,@NotBlank String amount,@NotNull Boolean isCredit) throws WalletException;

}