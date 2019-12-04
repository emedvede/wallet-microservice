package com.company.wallet.service;

import com.company.wallet.entities.Currency;
import com.company.wallet.entities.Transaction;
import com.company.wallet.entities.TransactionType;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exceptions.ErrorCode;
import com.company.wallet.exceptions.ErrorMessage;
import com.company.wallet.exceptions.WalletException;
import com.company.wallet.repository.CurrencyRepository;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.repository.TransactionTypeRepository;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.validator.Validator;
import com.company.wallet.validator.ValidatorImpl;
import org.hibernate.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.company.wallet.exceptions.ErrorMessage.NUMBER_FORMAT_MISMATCH;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

/**
 * TransactionService tests
 *
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
public class TransactionServiceTest {
    @TestConfiguration
    static class TransactionServiceImplTestContextConfiguration {
        @Bean
        public TransactionService transactionService() {
            return new TransactionServiceImpl();
        }

        @Bean
        public Validator validator() {
            return new ValidatorImpl();
        }
        //for annotation validation on method signature
        @Bean
        public MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }
    public static final String TEST_CURRENCY = "EUR";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";
    static int globalIdCounter = 1;

    public static final Integer CURRENCY_ID = 1;

    @Value("${db.updated_by}")
    String lastUpdatedBy;

    @Value("${application.transactionCredit.type.credit}")
    String credit;

    @Value("${application.transactionCredit.type.debit}")
    String debit;

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CurrencyRepository currencyRepository;

    @MockBean
    private TransactionTypeRepository transactionTypeRepository;

    @MockBean
    private WalletService walletService;

    private Currency currency;
    private Wallet wallet1;
    private Wallet wallet2;
    private TransactionType typeCredit;
    private TransactionType typeDebit;
    private Transaction transactionCredit;
    private Transaction transactionDebit;

    @Before
    public void setUp() throws WalletException {
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY, LAST_UPDATED_BY);
        wallet1 = new Wallet(USER,currency, new BigDecimal(0), LAST_UPDATED_BY);
        wallet1.setId(1);
        wallet2 = new Wallet(USER,currency, new BigDecimal(40), LAST_UPDATED_BY);
        wallet2.setId(2);
        typeCredit = new TransactionType(credit,"credit trn", LAST_UPDATED_BY);
        typeDebit = new TransactionType(debit,"debit trn", LAST_UPDATED_BY);
        transactionCredit = new Transaction(String.valueOf(globalIdCounter++) ,typeCredit,new BigDecimal(20),wallet1,currency,"Credit transaction");
        transactionCredit.setId(5);
        transactionDebit = new Transaction(String.valueOf(globalIdCounter++) ,typeDebit,new BigDecimal(20),wallet2,currency,"Debit transaction");
        transactionDebit.setId(6);


        //getTransactionsByWalletId
        Mockito.when(walletService.findById(wallet1.getId())).thenReturn(wallet1);
        Mockito.when(transactionRepository.findByWallet(wallet1))
                .thenReturn(Arrays.asList(transactionCredit));

        Mockito.when(transactionRepository.findByWallet(wallet2))
                .thenReturn(Arrays.asList(transactionCredit));


        //createTransaction
        Currency wrong = new Currency(2, "Wrong",LAST_UPDATED_BY);
        Mockito.when(currencyRepository.findByName("Wrong")).thenReturn(wrong);
        Mockito.when(walletRepository.save(new Wallet(USER,wrong, new BigDecimal(0), LAST_UPDATED_BY))).thenThrow(new ObjectNotFoundException("",""));

        Mockito.when(currencyRepository.findByName(TEST_CURRENCY)).thenReturn(currency);
        Mockito.when(transactionTypeRepository.getOne(typeCredit.getId())).thenReturn(typeCredit);
        Mockito.when(transactionTypeRepository.getOne(typeDebit.getId())).thenReturn(typeDebit);
        Mockito.when(walletService.findById(wallet1.getId())).thenReturn(wallet1);
        Mockito.when(walletService.findById(wallet2.getId())).thenReturn(wallet2);
        Mockito.when(walletService.findById(1001)).thenReturn(null);
    }

    //public List<Transaction> getTransactionsByWalletId(@NotNull Integer walletId) throws WalletException;
    @Test
    public void testGetTransactionsByWalletId_Success() throws WalletException {
        List<Transaction> found = transactionService.getTransactionsByWalletId(wallet1.getId());
        assertNotNull(found);
        assertTrue(found.size() == 1);
        assertTrue(found.get(0).getId().equals(transactionCredit.getId()) );
     }

    @Test
    public void testGetTransactionsByWalletId_Failed() throws WalletException {
        String error = String.format(ErrorMessage.NO_WALLET_FOUND,wallet2.getId().toString());
        Mockito.when(walletService.findById(wallet2.getId())).thenThrow(new WalletException(error,ErrorCode.BadRequest.getCode()));
        try {
            List<Transaction> found = transactionService.getTransactionsByWalletId(wallet2.getId());
            fail();
        } catch (WalletException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NO_WALLET_FOUND,wallet2.getId().toString()));
            assertEquals(ex.getErrorCode(),ErrorCode.BadRequest.getCode());
        }
    }

    //public Transaction createTransaction(@NotBlank String globalId, @NotBlank  String currency, @NotBlank String walletId, @NotBlank String transactionTypeId, @NotBlank String amount, String description) throws WalletException;
    @Test
    public void testCreateTransaction_SuccessCredit() throws WalletException {
        int amount = 100;
        Mockito.when(walletService.updateWalletAmount(wallet1,String.valueOf(amount),true)).thenReturn(wallet1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = globalIdCounter++;
        Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),wallet1.getId().toString(),typeCredit.getId(),String.valueOf(amount),"Success trn");
        assertNotNull(found);
        assertTrue(found.getId().equals(transactionCredit.getId()) );
    }

    @Test
    public void testCreateTransaction_SuccessDebit() throws WalletException {
        int amount = -10;
        Mockito.when(walletService.updateWalletAmount(wallet2,String.valueOf(amount),false)).thenReturn(wallet2);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
        int counter = globalIdCounter++;
        Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),wallet2.getId().toString(), typeDebit.getId(),String.valueOf(amount),"Success trn");
        assertNotNull(found);
        assertTrue(found.getId().equals(transactionDebit.getId()) );
    }

    @Test
    public void testCreateTransaction_DebitFailure() throws WalletException {
        int amount = -100;
        int counter = globalIdCounter++;
        String error = String.format(ErrorMessage.NOT_ENOUGH_FUNDS,wallet2.getId(),String.valueOf(amount));
        Mockito.when(walletService.updateWalletAmount(wallet2,String.valueOf(amount),false)).
                thenThrow(new WalletException(error, ErrorCode.BadRequest.getCode()));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionDebit);
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),wallet2.getId().toString(), typeDebit.getId(),String.valueOf(amount),"Success trn");
            fail();
        } catch (WalletException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NOT_ENOUGH_FUNDS,wallet2.getId(),String.valueOf(amount)));
            assertEquals(ex.getErrorCode(),ErrorCode.BadRequest.getCode());
        }
    }

    @Test
    public void testCreateTransaction_WalletNotFound() throws WalletException {
        int amount = 100;
        int counter = globalIdCounter++;
        String notFoundWalletId = "1001";
        Mockito.when(walletService.updateWalletAmount(wallet1,String.valueOf(amount),true)).thenReturn(wallet1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter),currency.getName(),notFoundWalletId,typeCredit.getId(),String.valueOf(amount),"No wallet");
            fail();
        } catch (WalletException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NO_WALLET_FOUND, notFoundWalletId));
            assertEquals(ex.getErrorCode(),ErrorCode.BadRequest.getCode());
        }
    }


    @Test
    public void testCreateTransaction_AmountNotNumber() throws WalletException {
        String wrongAmount = "AAAee";
        Mockito.when(walletService.updateWalletAmount(wallet1,String.valueOf(wrongAmount),true)).thenReturn(wallet1);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionCredit);
        int counter = globalIdCounter++;
        try {
            Transaction found = transactionService.createTransaction(String.valueOf(counter), currency.getName(), wallet1.getId().toString(), typeCredit.getId(), wrongAmount, "Fail trn");
        }catch (WalletException ex){
            assertEquals(ex.getMessage(),String.format(NUMBER_FORMAT_MISMATCH,wrongAmount));
            assertEquals(ex.getErrorCode(),ErrorCode.BadRequest.getCode());
        }
    }
}
