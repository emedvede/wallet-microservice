package com.company.wallet.service;

import com.company.wallet.entities.Currency;
import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.TransactionType;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exceptions.ErrorMessage;
import com.company.wallet.exceptions.WalletException;
import com.company.wallet.repository.CurrencyRepository;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.repository.TransactionTypeRepository;
import com.company.wallet.helper.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.validation.annotation.Validated;

//import javax.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

import static com.company.wallet.exceptions.ErrorMessage.NUMBER_FORMAT_MISMATCH;

@Validated
@PropertySource("classpath:application.properties")
@Service
public class TransactionServiceImpl implements TransactionService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private Helper inputParametersValidator;


    @Value("${db.updated_by}")
    private String updatedBy;

    @Value("${application.transaction.type.credit}")
    private String transactionTypeCredit;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getTransactionTypeCredit() {
        return transactionTypeCredit;
    }

    public void setTransactionTypeCredit(String transactionTypeCredit) {
        this.transactionTypeCredit = transactionTypeCredit;
    }

    @Transactional(rollbackFor = WalletException.class)
    @Override
    public List<Transaction> getTransactionsByWalletId(@NotNull Integer walletId) throws WalletException {
        Wallet wallet = walletService.findById(walletId);
        if(wallet != null) {
            return transactionRepository.findByWallet(wallet);
        } else {
            throw new WalletException(String.format(ErrorMessage.NO_WALLET_FOUND,walletId.toString()), HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Creates transaction for wallet.
     * If there is not enough funds on wallet balance, throws WalletException
     * If transactionTypeId='C' (credit transaction), takes absolute amount from  @param amount  and adds it to wallet balance.
     * If transactionTypeId='D' (debit transaction), takes absolute amount from  @param amount  and subtracts it from wallet balance.
     * Valid refence to transaction type, currency, wallet should be provided.
     * Global id should be unique.
     * Transaction should have the same currency as wallet.
     * No additional SQL query is used to select currency by Id and transaction type by Id
     * because JPARepository.getOne is used, which returns only reference for transaction object.
     *
     * Set isolation = Isolation.SERIALIZABLE in order to avoid concurrency issues (in case of deploying application to multiple hosts)
     *
     * @param globalId unique global id
     * @param currencyName valid currency name
     * @param walletId valid wallet id
     * @param transactionTypeId valid transaction type - 'C' or 'D'
     * @param amount transaction amount
     * @param description
     * @return created transaction
     * @throws WalletException if couldn't create transaction
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = WalletException.class)
    @Override
    public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currencyName, @NotBlank String walletId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws WalletException{
        try {
            //Check for unique transaction globalId happens due to entity constrains on Transaction.globalId (unique=true)

            //Get currency reference
            Currency currency = currencyRepository.findByName(currencyName);
            String error = String.format(ErrorMessage.NO_CURRENCY_PRESENT, currencyName);
            inputParametersValidator.conditionIsTrue(currency != null,error,HttpStatus.BAD_REQUEST.value());

            //Get transactionType reference
            TransactionType transactionType = transactionTypeRepository.getOne(transactionTypeId);

            //Check wallet is present
            Wallet wallet = walletService.findById(Integer.valueOf(walletId));
            error = String.format(ErrorMessage.NO_WALLET_FOUND, walletId);
            inputParametersValidator.conditionIsTrue(wallet != null,error,HttpStatus.BAD_REQUEST.value());

            //check that transaction and wallet have the same currency
            error = String.format(ErrorMessage.TRANSACTION_CURRENCY_NOT_EQ_WALLET_CURRENCY,currency.getName(), wallet.getCurrency().getName());
            inputParametersValidator.conditionIsTrue(wallet.getCurrency().getId().equals(currency.getId()),error,HttpStatus.BAD_REQUEST.value());

            //Update wallet, checks if there is enough funds for debit transaction. If not, throws WalletException
            wallet = walletService.updateWalletAmount(wallet,amount,transactionTypeId.equalsIgnoreCase(transactionTypeCredit));

            //Create transaction
            Transaction transaction = new Transaction(globalId,transactionType,new BigDecimal(amount),wallet,currency,description,updatedBy);

            return transactionRepository.save(transaction);

        } catch(NumberFormatException e){
            throw new WalletException(String.format(NUMBER_FORMAT_MISMATCH,amount),HttpStatus.BAD_REQUEST.value());
        }

    }
}
