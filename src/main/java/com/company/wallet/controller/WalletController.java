package com.company.wallet.controller;

import com.company.wallet.entities.Wallet;
import com.company.wallet.exceptions.WalletException;
import com.company.wallet.gson.adapter.HibernateProxyTypeAdapter;
import com.company.wallet.gson.exclusion.ExcludeField;
import com.company.wallet.gson.exclusion.GsonExclusionStrategy;
import com.company.wallet.validator.Validator;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import com.company.wallet.service.WalletService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Restful controller for managing wallets
 *  @author Elena Medvedeva
 */
@RestController
class WalletController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WalletService walletService;

    @Autowired
    private Validator validator;

    @GetMapping(
            value = "/test",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ResponseBody
    public String test() throws WalletException, ClassNotFoundException {
        return "Hello from wallet microservice!";
    }


    @GetMapping(
    value = "/wallets",
    produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getAll() throws WalletException, ClassNotFoundException {
        logger.debug("Called WalletController.getAll");
        return new GsonBuilder().setExclusionStrategies(new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTIONS))
                .create().toJson(walletService.findAll());
    }

    @GetMapping(
            value = "/wallets/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getWalletById( @PathVariable("id") int id) throws WalletException, ClassNotFoundException {
        logger.debug("Called WalletController.getWalletById with id=" + id);
        Wallet wallet = walletService.findById(id);
        return new GsonBuilder().setExclusionStrategies(new GsonExclusionStrategy(ExcludeField.EXCLUDE_WALLET))
                .create().toJson(wallet);
    }

    @GetMapping(
            value = "/wallets/user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String getWalletsByUserId( @RequestParam("userId") String userId) throws WalletException, ClassNotFoundException {
        logger.debug("Called WalletController.getWalletsByUserId with userId=" + userId);
        List<Wallet> wallets = walletService.findByUserId(userId);
        return new GsonBuilder().setExclusionStrategies(new GsonExclusionStrategy(ExcludeField.EXCLUDE_TRANSACTIONS))
                .create().toJson(wallets);
    }


    /**
     * Creates new wallet.currency must be provided. In the form {"userId":"user",currency":"EUR"}
     * @param dataHashMap Expecting currency to be set, e. g. {"userId":"user","currency":"EUR"}. Expects hashmap in JSON format.
     * @return new wallet in JSON format
     * @throws WalletException when failed to create wallet
     */
    @PostMapping(value = "/wallets",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createWallet(@RequestBody HashMap<String, String> dataHashMap) throws WalletException {
        logger.debug("Called WalletController.createWallet");
        validator.validate(dataHashMap,Arrays.asList("userId","currency"));
        Wallet wallet = walletService.createWallet(dataHashMap.get("userId"),dataHashMap.get("currency"));
        return new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).create().toJson(wallet);
    }

}
