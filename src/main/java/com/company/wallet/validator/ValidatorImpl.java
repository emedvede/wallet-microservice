package com.company.wallet.validator;

import com.company.wallet.exceptions.ErrorCode;
import com.company.wallet.exceptions.WalletException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.company.wallet.exceptions.ErrorMessage.NO_MANDATORY_FIELD;

/**
 * Validator to check that mandatory parameters are present and some condition is TRUE.
 * @author Elena Medvedeva
 */
@Validated
@Component
public class ValidatorImpl implements Validator<String,String>{

    @Override
    public void validate(@NotNull Map<String, String> input, @NotNull List<String> required) throws WalletException {
        for (String parameter : required) {
            if (!input.containsKey(parameter) || (input.get(parameter) == null) || (input.get(parameter).isEmpty())) {
                String message = String.format(NO_MANDATORY_FIELD, parameter);
                throw new WalletException(message, ErrorCode.BadRequest.getCode());
            }
        }
    }

    @Override
    public void isTrue(@NotNull Boolean condition,@NotNull String errorMessage, int errorCode) throws WalletException{
        if(!condition){
            throw new WalletException(errorMessage, errorCode);
        }
    }
}
