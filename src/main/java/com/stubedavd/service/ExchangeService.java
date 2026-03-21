package com.stubedavd.service;

import com.stubedavd.repository.JdbcExchangeRateRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.exception.InfrastructureException;

import java.math.BigDecimal;
import java.util.Optional;

public class ExchangeService {
    private static final int ZERO_ID = 1;
    public static final String USD_CODE = "USD";

    public ExchangeRate findExchangeRate(ExchangeRate exchangeRate) throws InfrastructureException {
        ExchangeRate result = null;
        if (exchangeRate != null) {
            JdbcExchangeRateRepository dao = new JdbcExchangeRateRepository();
            Currency baseCurrency = exchangeRate.getBaseCurrency();
            Currency targetCurrency = exchangeRate.getTargetCurrency();
            Optional<ExchangeRate> reverseExchangeRate = dao.findByCodes(targetCurrency.getCode(), baseCurrency.getCode());
            if (reverseExchangeRate.isPresent()) {
                BigDecimal rate = BigDecimal.ONE.divide(reverseExchangeRate.get().getRate(), 6, BigDecimal.ROUND_HALF_UP);
                result = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, rate);
            } else {
                Optional<ExchangeRate> exchangeRateUSDtoBaseCurrency = dao.findByCodes(USD_CODE, baseCurrency.getCode());
                Optional<ExchangeRate> exchangeRateUSDtoTargetCurrency = dao.findByCodes(USD_CODE, targetCurrency.getCode());
                if (exchangeRateUSDtoBaseCurrency.isPresent() && exchangeRateUSDtoTargetCurrency.isPresent()) {
                    BigDecimal baseRateToUSD = BigDecimal.ONE.divide(exchangeRateUSDtoBaseCurrency.get().getRate(), 6, BigDecimal.ROUND_HALF_UP);
                    BigDecimal resultRate = baseRateToUSD.multiply(exchangeRateUSDtoTargetCurrency.get().getRate());
                    result = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, resultRate);
                }
            }
        }
        return result;
    }
}
