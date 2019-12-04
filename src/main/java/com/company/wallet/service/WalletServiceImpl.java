package com.company.wallet.service;

import com.company.wallet.entities.Currency;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exceptions.ErrorCode;
import com.company.wallet.exceptions.ErrorMessage;
import com.company.wallet.exceptions.WalletException;
import com.company.wallet.repository.CurrencyRepository;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.validator.InputParametersValidator;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.validation.annotation.Validated;

import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
/**
 * Service for managing wallets
 * @author Elena Medvedeva
 */
@Validated
@PropertySource("classpath:application.properties")
@Service
class WalletServiceImpl implements WalletService{

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private InputParametersValidator inputParametersValidator;

    @Value("${db.updated_by}")
    private String updatedBy;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Transactional(rollbackFor = WalletException.class)
    @Override
    public List<Wallet> findAll() throws WalletException {
        return walletRepository.findAllByOrderByIdAsc();
    }

    @Transactional(rollbackFor = WalletException.class)
    @Override
    public Wallet findById(@NotNull Integer id) throws WalletException {
       Optional<Wallet> optionalWallet =  walletRepository.findById(id);
       //validate
       inputParametersValidator.conditionIsTrue(optionalWallet.isPresent(),String.format(ErrorMessage.NO_WALLET_FOUND,id.toString()),ErrorCode.BadRequest.getCode());
       return optionalWallet.get();
    }

    @Transactional(rollbackFor = WalletException.class)
    @Override
    public List<Wallet> findByUserId(@NotBlank String userId) throws WalletException {
        return walletRepository.findByUserId(userId);
    }

    /**
     * Creates wallet based on currency.
     * No additional SQL query is used to select currency by Id,
     * because JPARepository.getOne is used, which returns only reference for currency object.
     * @param userId valid currency id
     * @param currencyName valid currency name
     * @return created wallet
     * @throws WalletException
     */
    @Transactional(rollbackFor = WalletException.class)
    @Override
    public Wallet createWallet(@NotBlank String userId,@NotBlank String currencyName) throws WalletException{
        try {
            //Currency currency = currencyRepository.getOne(currencyId);
            Currency currency = currencyRepository.findByName(currencyName);
            String error = String.format(ErrorMessage.NO_CURRENCY_PRESENT,currencyName);
            inputParametersValidator.conditionIsTrue(currency != null,error,ErrorCode.BadRequest.getCode());
            return walletRepository.save(new Wallet(userId, currency, new BigDecimal(0), updatedBy));
        } catch (ObjectNotFoundException e){
            throw new WalletException(String.format(ErrorMessage.NO_CURRENCY_PRESENT,currencyName),ErrorCode.BadRequest.getCode());
        }
    }

    /**
     * Updates wallet balance. Prior to it checks if there is enough funds on wallet balance.
     * If there is not enough funds, throws WalletException
     * If isCredit is set to true, takes absolute amount from  @param amount  and adds it to wallet balance.
     * If isCredit is set to false, takes absolute amount from  @param amount  and subtracts it from wallet balance.
     *
     * Set isolation = Isolation.SERIALIZABLE in order to avoid concurrency issues (in case of deploying application to multiple hosts)
     * This will slow down performance.
     * @param wallet
     * @param amount
     * @param isCredit
     * @return updated wallet
     * @throws WalletException if couldn't update wallet balance, e.g. not enough funds.
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = WalletException.class)
    @Override
    public Wallet updateWalletAmount(@NotNull Wallet wallet,@NotBlank String amount,@NotNull  Boolean isCredit) throws WalletException{
        try {
            BigDecimal transactionAmount = (isCredit) ? new BigDecimal(amount).abs() : new BigDecimal(amount).abs().negate();

            //check that there is enough funds on wallet balance for debit transaction
            Boolean condition = (isCredit || (wallet.getBalance().compareTo(transactionAmount.abs()) >= 0) );
            inputParametersValidator.conditionIsTrue(condition, String.format(ErrorMessage.NOT_ENOUGH_FUNDS,wallet.getId(),amount),ErrorCode.BadRequest.getCode());

            //update wallet
            wallet.setBalance(wallet.getBalance().add(transactionAmount));
            wallet.setLastUpdatedBy(updatedBy);
            wallet.setLastUpdated(new Date());

            return walletRepository.save(wallet);

        }catch (NumberFormatException e){
            String error = String.format(ErrorMessage.NUMBER_FORMAT_MISMATCH,amount);
            throw new WalletException(error, ErrorCode.BadRequest.getCode());
        }
    }
}
