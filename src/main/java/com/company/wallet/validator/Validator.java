package com.company.wallet.validator;

import com.company.wallet.exceptions.WalletException;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Validator to validate input parameters and check that condition is TRUE.
 * @param <K>
 * @param <V>
 *
 * @author Elena Medvedeva
 */
public interface Validator<K,V> {
    public void validate(@NotNull Map<K, V> input, @NotNull List<K> required) throws WalletException;
    public void isTrue(@NotNull Boolean condition, @NotNull String errorMessage, int errorCode) throws WalletException;

}
