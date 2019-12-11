package com.company.wallet.service;

import com.company.wallet.entities.Currency;
import com.company.wallet.entities.Wallet;
import com.company.wallet.exceptions.ErrorMessage;
import com.company.wallet.exceptions.WalletException;
import com.company.wallet.repository.CurrencyRepository;
import com.company.wallet.repository.TransactionRepository;
import com.company.wallet.repository.WalletRepository;
import com.company.wallet.helper.Helper;
import com.company.wallet.helper.HelperImpl;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.junit.Assert.*;
/**
 * WalletService tests
 *
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
public class WalletServiceTest {
    @TestConfiguration
    static class WalletServiceImplTestContextConfiguration {
        @Bean
        public WalletService walletService() {
            return new WalletServiceImpl();
        }

        @Bean
        public Helper validator() {
            return new HelperImpl();
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
    public static final Integer CURRENCY_ID = 1;

    @Value("${db.updated_by}")
    String lastUpdatedBy;

    @Autowired
    private WalletService walletService;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private CurrencyRepository currencyRepository;

    Currency currency;
    Wallet wallet1;
    Wallet wallet2;

    @Before
    public void setUp() {
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY, LAST_UPDATED_BY);
        wallet1 = new Wallet(USER,currency, new BigDecimal(0), LAST_UPDATED_BY);
        wallet1.setId(1);
        wallet2 = new Wallet(USER,currency, new BigDecimal(20), LAST_UPDATED_BY);
        wallet2.setId(2);

        //walletService.findAll
        Mockito.when(walletRepository.findAllByOrderByIdAsc())
                .thenReturn(Arrays.asList(wallet1, wallet2));
        //findById
        Mockito.when(walletRepository.findById(wallet1.getId())).thenReturn(Optional.of(wallet1));
        Mockito.when(walletRepository.findById(110)).thenReturn(Optional.empty());
        //
        //walletService.findUserId
        Mockito.when(walletRepository.findByUserId(USER))
                .thenReturn(Arrays.asList(wallet1, wallet2));
        Mockito.when(walletRepository.findByUserId("test"))
                .thenReturn(new ArrayList<Wallet>());

        //createWallet
        Currency wrong = new Currency(2, "Wrong",LAST_UPDATED_BY) ;
        Mockito.when(currencyRepository.findByName("Wrong")).thenReturn(wrong);
        Mockito.when(walletRepository.save(new Wallet(USER,wrong, new BigDecimal(0), LAST_UPDATED_BY))).thenThrow(new ObjectNotFoundException("",""));
        Mockito.when(currencyRepository.findByName(TEST_CURRENCY)).thenReturn(currency);
        Mockito.when(walletRepository.save(wallet1)).thenReturn(wallet1);
        Mockito.when(walletRepository.save(wallet2)).thenReturn(wallet2);
    }

    //public List<Wallet> findAll() throws WalletException;

    @Test
    public void testFindAll() throws WalletException {
        List<Wallet> found = walletService.findAll();
        assertNotNull(found);
        assertTrue(found.size() == 2);
        assertTrue(found.get(0).getId().equals(wallet1.getId()) );
        assertTrue(found.get(1).getId().equals(wallet2.getId()) );
    }

    //public Wallet findById(@NotNull Integer id) throws WalletException;
    @Test
    public void testFindById_Success() throws WalletException {
        Wallet found = walletService.findById(wallet1.getId());
        assertNotNull(found);
        assertTrue(found.getId().equals(wallet1.getId()) );
    }

    @Test(expected = ConstraintViolationException.class)
    public void testFindById_Null() throws WalletException {
        Wallet found = walletService.findById(null);
    }

    @Test
    public void testFindById_DoesntExist() throws WalletException {
        try {
            Wallet found = walletService.findById(110);
            assertNull(found);
            fail();
        }catch(WalletException e){
            assertEquals(e.getMessage(),String.format(ErrorMessage.NO_WALLET_FOUND,"110"));
            assertEquals(e.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }
    // public List<Wallet> findByUserId(@NotBlank String userId) throws WalletException;
    @Test
    public void testFindByUserId_Success() throws WalletException {
        List<Wallet> found = walletService.findByUserId(wallet1.getUserId());
        assertNotNull(found);
        assertTrue(found.size() == 2 );
    }

    @Test(expected = ConstraintViolationException.class)
    public void testFindByUserId_Null() throws WalletException {
        walletService.findByUserId(null);
    }

    @Test
    public void testFindByUserId_DoesntExist() throws WalletException {
            List<Wallet> found = walletService.findByUserId("test");
            assertNotNull(found);
            assertTrue(found.size() == 0);
    }

    //public Wallet createWallet(@NotBlank String currency) throws WalletException;

    @Test(expected = ConstraintViolationException.class)
    public void testCreateWallet_Null() throws WalletException {
        Wallet found = walletService.createWallet(USER,null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateWallet_Blank() throws WalletException {
        Wallet found = walletService.createWallet(USER,"");
    }

    @Test
    public void testCreateWallet_CurrencyNotFound() throws WalletException {
        try {
        Wallet found = walletService.createWallet(USER,"Wrong");
        }catch(WalletException e){
            assertEquals(e.getMessage(),String.format(ErrorMessage.NO_CURRENCY_PRESENT,"Wrong"));
            assertEquals(e.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

    @Test
    public void testCreateWallet_Success() throws WalletException {
        Mockito.when(walletRepository.save(Mockito.any(Wallet.class))).thenReturn(wallet1);
        Wallet found = walletService.createWallet(USER,TEST_CURRENCY);
        assertEquals(found.getId(),wallet1.getId());
    }

    //   public Wallet updateWalletAmount(@NotNull Wallet wallet,@NotNull String amount,@NotNull Boolean isCredit) throws WalletException;

    @Test
    public void testUpdateWalletAmount_isCredit() throws WalletException {
        int amount = 30;
        Wallet found = walletService.updateWalletAmount(wallet1,String.valueOf(amount),true);
        assertEquals(found.getId(),wallet1.getId());
        assertEquals(found.getBalance(),new BigDecimal(amount));
    }

    @Test
    public void testUpdateWalletAmount_isDebitSuccess() throws WalletException {
        int amount = 10;
        Wallet found = walletService.updateWalletAmount(wallet2,String.valueOf(amount),false);
        assertEquals(found.getId(),wallet2.getId());
        assertEquals(found.getBalance(),new BigDecimal(10));
    }

    @Test
    public void testUpdateWalletAmount_isDebitSuccess2() throws WalletException {
        int amount = -10;
        Wallet found = walletService.updateWalletAmount(wallet2,String.valueOf(amount),false);
        assertEquals(found.getId(),wallet2.getId());
        assertEquals(found.getBalance(),new BigDecimal(10));
    }

    @Test
    public void testUpdateWalletAmount_isDebitFailure() throws WalletException {
        int amount = 100;
        try {
            Wallet found = walletService.updateWalletAmount(wallet2, String.valueOf(amount), false);
            fail();
        } catch (WalletException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NOT_ENOUGH_FUNDS,wallet2.getId(),String.valueOf(amount)));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }

    @Test
    public void testUpdateWalletAmount_AmountNotANumber() throws WalletException {
        String badAmount = "STTTT";
        try {
            Wallet found = walletService.updateWalletAmount(wallet2, badAmount, false);
            fail();
        } catch (WalletException ex){
            assertEquals(ex.getMessage(),String.format(ErrorMessage.NUMBER_FORMAT_MISMATCH,badAmount));
            assertEquals(ex.getErrorCode(),HttpStatus.BAD_REQUEST.value());
        }
    }
}
