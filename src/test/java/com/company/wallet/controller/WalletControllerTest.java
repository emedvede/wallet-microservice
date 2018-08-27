package com.company.wallet.controller;

import com.company.wallet.entities.Currency;
import com.company.wallet.entities.Wallet;
import com.company.wallet.service.WalletService;
import com.company.wallet.validator.Validator;
import com.company.wallet.validator.ValidatorImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


import static com.company.wallet.exceptions.ErrorMessage.NO_MANDATORY_FIELD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WalletController tests
 * @author Elena Medvedeva
 */
@RunWith(SpringRunner.class)
@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @TestConfiguration
    static class WalletControllerTestContextConfiguration {
        @Bean
        public Validator validator() {
            return new ValidatorImpl();
        }

    }
    public static final Integer CURRENCY_ID = 1;
    public static final String TEST_CURRENCY = "EUR";
    public static final String LAST_UPDATED_BY = "user";
    public static final String USER = "user";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WalletService service;

    private Currency currency;
    private Wallet wallet;

    @Before
    public void before(){
        currency = new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY );
        wallet = new Wallet(USER,new Currency(CURRENCY_ID, TEST_CURRENCY,LAST_UPDATED_BY),new BigDecimal(0),LAST_UPDATED_BY);
        wallet.setId(1);
    }

    @Test
    public void testGetAll_whenGetWallet_thenReturnJsonArray() throws Exception {
        List<Wallet> allWallets = Arrays.asList(wallet);

        given(service.findAll()).willReturn(allWallets);

        mvc.perform(get("/wallets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(wallet.getId())));
    }

    @Test
    public void testGetWalletById_thenReturnJson() throws Exception {

        given(service.findById(wallet.getId())).willReturn(wallet);

        mvc.perform(get("/wallets/" + wallet.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(wallet.getId())))
                .andExpect(jsonPath("$.userId", is(wallet.getUserId())))
                .andExpect(jsonPath("$.currency.id", is(wallet.getCurrency().getId())))
                .andExpect(jsonPath("$.balance", is(wallet.getBalance().intValue())))
                .andExpect(jsonPath("$.lastUpdatedBy", is(wallet.getLastUpdatedBy())));
    }

    @Test
    public void testGetWalletByUserId_thenReturnJson() throws Exception {

        given(service.findByUserId(wallet.getUserId())).willReturn(Arrays.asList(wallet));

        mvc.perform(get("/wallets/user")
                .param("userId",wallet.getUserId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(wallet.getUserId())));

    }

    @Test
    public void testCreateWallet_thenReturnJson() throws Exception {

        given(service.createWallet(USER,TEST_CURRENCY)).willReturn(wallet);
        String validCurrencyJson = "{\"userId\":\"" + USER +"\",\"currency\":\"" + TEST_CURRENCY + "\"}";

        mvc.perform(post("/wallets")
                .content(validCurrencyJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(wallet.getId())))
                .andExpect(jsonPath("$.userId", is(wallet.getUserId())))
                .andExpect(jsonPath("$.currency.name", is(TEST_CURRENCY)))
                .andExpect(jsonPath("$.balance", is(wallet.getBalance().intValue())))
                .andExpect(jsonPath("$.lastUpdatedBy", is(wallet.getLastUpdatedBy())));
    }

    @Test
    public void testCreateWallet_NoCurrency() throws Exception {

        given(service.createWallet(USER,currency.getName())).willReturn(wallet);
        String json = "{\"userId\":\"" + USER +"\"}";
        String errorMessage = String.format(NO_MANDATORY_FIELD, "currency");

        mvc.perform(post("/wallets")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/wallets")));
    }

    @Test
    public void testCreateWallet_NoUserId() throws Exception {

        given(service.createWallet(USER,currency.getName())).willReturn(wallet);
        String json = "{\"currency\":\"" + TEST_CURRENCY +"\"}";
        String errorMessage = String.format(NO_MANDATORY_FIELD, "userId");

        mvc.perform(post("/wallets")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.details", is("uri=/wallets")));
    }

    @Test
    public void testCreateWallet_MalformedJson() throws Exception {

        given(service.createWallet(USER,currency.getName())).willReturn(wallet);
        String json = "{mmmm";
        String errorMessage = String.format(NO_MANDATORY_FIELD, "currency");

        mvc.perform(post("/wallets")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("JSON parse error: Unexpected character")))
                .andExpect(jsonPath("$.details", is("uri=/wallets")));
    }

}
