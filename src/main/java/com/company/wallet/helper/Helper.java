package com.company.wallet.helper;

import com.company.wallet.exceptions.WalletException;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Helper to check that condition is TRUE.
 * @param <K>
 * @param <V>
 *
 * @author Elena Medvedeva
 */
public interface Helper<K,V> {
    public void conditionIsTrue(@NotNull Boolean condition, @NotNull String errorMessage, int errorCode) throws WalletException;

}
