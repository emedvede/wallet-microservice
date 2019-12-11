package com.company.wallet.exceptions;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Class to store custom error messages.
 *  Also generates some error messages based on exception messages
 *
 *  @author Elena Medvedeva
 */
public class ErrorMessage {
    //Messages to display to user
    public static final String NO_CURRENCY_PRESENT = "No currency %s exists in the system.";
    public static final String MALFORMED_CURRENCY = "Field currency is invalid.";
    public static final String NO_WALLET_FOUND = "No wallet with id %s exists in the system.";
    public static final String ARGUMENT_TYPE_MISMATCH = "%s should be of type %s";
    public static final String NO_CURRENCY = "No field 'currency' provided";
    public static final String TRANSACTION_WITH_GLOBAL_ID_PRESENT = "Transaction with globalId=%s already present.";
    public static final String NO_TRANSACTION_TYPE_PRESENT = "Undefined transactionType %s.";
    public static final String NUMBER_FORMAT_MISMATCH = "'%s' should be a number";
    public static final String NOT_ENOUGH_FUNDS = "Wallet %d has not enough funds to perform debit transaction with amount %s";
    public static final String PART_NO_MANDATORY_FIELD = " is mandatory. It should be provided and can't be empty.";
    public static final String NO_MANDATORY_FIELD = "Field %s" + PART_NO_MANDATORY_FIELD;
    public static final String TRANSACTION_CURRENCY_NOT_EQ_WALLET_CURRENCY = "Transaction can't be saved. Transaction currency %s differs from wallet currency %s.";

    //Template messages to compare
    public static final String DUPLICATE_KEY = "duplicate key value violates unique constraint \"transaction_global_id_key\"";
    public static final String CURRENCY_FK_VIOLATES_TRANSACTION = "violates foreign key constraint \"transaction_currency_id_fkey\"";
    public static final String CURRENCY_FK_VIOLATES_WALLET = "violates foreign key constraint \"wallet_currency_id_fkey\"";
    public static final String CURRENCY_TOO_LONG = "value too long";
    public static final String TYPE_FK_VIOLATES_TRANSACTION = "violates foreign key constraint \"transaction_type_id_fkey\"";
    public static final String WALLET_FK_VIOLATES_TRANSACTION = "violates foreign key constraint \"transaction_wallet_id_fkey\"";

    public static final String [][] ERRORS = {
            {DUPLICATE_KEY,                         TRANSACTION_WITH_GLOBAL_ID_PRESENT},
            {CURRENCY_FK_VIOLATES_TRANSACTION,      NO_CURRENCY_PRESENT },
            {CURRENCY_FK_VIOLATES_WALLET,           NO_CURRENCY_PRESENT},
            {CURRENCY_TOO_LONG,                     MALFORMED_CURRENCY},
            {TYPE_FK_VIOLATES_TRANSACTION,          NO_TRANSACTION_TYPE_PRESENT},
            {WALLET_FK_VIOLATES_TRANSACTION,        NO_WALLET_FOUND}
    };

    /**
     * Generates error message besed on DataIntegrityViolationException error message using ErrorMessage.ERRORS
     * @param errorMessage from exception from DataIntegrityViolationException
     * @return Generated message for user
     */
    public static String generateErrorMessageForDataIntegrityViolationException(String errorMessage){
        String bodyOfResponse = errorMessage;
        //create map of array ERRORS
         Map<String, String> errors =
                Arrays.stream(ERRORS).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
         //check if error message contains any of ERRORS keys and generate corresponding response
         for(String key:errors.keySet()){
             if(errorMessage.contains(key)){
                 if(!key.equals(CURRENCY_TOO_LONG)) {
                     String id = getId(errorMessage);
                     bodyOfResponse = String.format(errors.get(key),id);
                 } else {
                     bodyOfResponse = errors.get(key);
                 }
             }
         }
         return bodyOfResponse;
    }


    private static String getId(String error){
        return error.substring(error.lastIndexOf("(") + 1,error.lastIndexOf(")"));
    }

}
