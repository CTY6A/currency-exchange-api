package com.stubedavd.service;

import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.exception.InfrastructureException;

import java.math.BigDecimal;

public class ExchangeService {
    private static final int ZERO_ID = 1;
    public static final String USD_CODE = "USD";

    public ExchangeRate findExchangeRate(ExchangeRate exchangeRate) throws InfrastructureException {
        ExchangeRate result = null;
        if (exchangeRate != null) {
            ExchangeRateRepository dao = new ExchangeRateRepository();
            Currency baseCurrency = exchangeRate.getBaseCurrency();
            Currency targetCurrency = exchangeRate.getTargetCurrency();
            ExchangeRate reverseExchangeRate = dao.findByPair(targetCurrency.getCode(), baseCurrency.getCode());
            if (reverseExchangeRate != null) {
                BigDecimal rate = BigDecimal.ONE.divide(reverseExchangeRate.getRate(), 6, BigDecimal.ROUND_HALF_UP);
                result = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, rate);
            } else {
                ExchangeRate exchangeRateUSDtoBaseCurrency = dao.findByPair(USD_CODE, baseCurrency.getCode());
                ExchangeRate exchangeRateUSDtoTargetCurrency = dao.findByPair(USD_CODE, targetCurrency.getCode());
                if (exchangeRateUSDtoBaseCurrency != null && exchangeRateUSDtoTargetCurrency != null) {
                    BigDecimal baseRateToUSD = BigDecimal.ONE.divide(exchangeRateUSDtoBaseCurrency.getRate(), 6, BigDecimal.ROUND_HALF_UP);
                    BigDecimal resultRate = baseRateToUSD.multiply(exchangeRateUSDtoTargetCurrency.getRate());
                    result = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, resultRate);
                }
            }
        }
        return result;
    }
}
