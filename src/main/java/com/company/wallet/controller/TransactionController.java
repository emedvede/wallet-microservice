package com.company.wallet.controller;

import com.company.wallet.entities.Transaction;
import com.company.wallet.exceptions.WalletException;
import com.company.wallet.gson.adapter.HibernateProxyTypeAdapter;
import com.company.wallet.gson.exclusion.ExcludeField;
import com.company.wallet.gson.exclusion.GsonExclusionStrategy;
import com.company.wallet.helper.Helper;
import com.company.wallet.service.TransactionService;
import com.company.wallet.view.model.TransactionModel;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Restful controller for managing wallet transactions
 *
 * @author Elena Medvedeva
 */
@RestController
public class TransactionController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private Helper inputParametersValidator;

    @GetMapping(
            value = "/wallets/{id}/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getWalletTransactionsById( @PathVariable("id") int id) throws WalletException, ClassNotFoundException {
        logger.debug("Called TransactionController.getWalletTransactionsById with parameter walletId={}",id);
        List<Transaction> transactionList = transactionService.getTransactionsByWalletId(id);
        return new GsonBuilder().
                setExclusionStrategies(new GsonExclusionStrategy(ExcludeField.EXCLUDE_WALLET)).
                create().toJson(transactionList);

    }

    /**
     * Creates wallet transaction.
     * <p>
     * Example of  credit transaction JSON body
     * {"globalId":"123","currency":"EUR","walletId": "1","transactionTypeId":"C","amount":"100","description":"add money"}
     *
     * Example of debit transaction JSON body
     * {"globalId":"123","currency":"EUR","walletId": "1","transactionTypeId":"D","amount":"100","description":"withdraw money"}
     * </p>
     * @param transactionModel contains input parameters in the following format:
     *                {"globalId":"123","currency":"EUR","walletId": "1","transactionTypeId":"C","amount":"100","description":"add money"}
     * @return created transaction in JSON format
     * @throws WalletException when couldn't create transaction (e.g. globalId not unique, not enough funds on wallet balance, etc.)
     * @throws ClassNotFoundException
     */

    @PostMapping(
            value = "/transactions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String createWalletTransaction(@Valid @RequestBody TransactionModel transactionModel) throws WalletException, ClassNotFoundException {
        logger.debug("Called TransactionController.createWalletTransaction" );


        Transaction transaction = transactionService.createTransaction(transactionModel.getGlobalId(),transactionModel.getCurrency(),transactionModel.getWalletId(),
                transactionModel.getTransactionTypeId(),transactionModel.getAmount(),transactionModel.getDescription());
        logger.info("Transaction created with id=" + transaction.getId() );

        return new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).
                setExclusionStrategies(new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTIONS)).
                create().toJson(transaction);
    }
}
