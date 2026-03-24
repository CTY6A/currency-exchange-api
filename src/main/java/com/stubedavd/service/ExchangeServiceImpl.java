package com.stubedavd.service;

import com.stubedavd.exception.NotFoundException;
import com.stubedavd.model.response.ExchangeResponse;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeServiceImpl implements ExchangeService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    public ExchangeServiceImpl(
            ExchangeRateRepository exchangeRateRepository,
            CurrencyRepository currencyRepository
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public ExchangeResponse getExchangeResponse(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal amount) {

        Optional<ExchangeResponse> exchangeResponse =
                convertDirect(baseCurrencyCode, targetCurrencyCode, amount);

        if (exchangeResponse.isEmpty()) {

            exchangeResponse = convertReverse(baseCurrencyCode, targetCurrencyCode, amount);

            if (exchangeResponse.isEmpty()) {

                exchangeResponse = convertViaUsd(baseCurrencyCode, targetCurrencyCode, amount);

                if (exchangeResponse.isEmpty()) {
                    throw new NotFoundException("Exchange could not be found");
                }
            }
        }

        return exchangeResponse.get();
    }

    private Optional<ExchangeResponse> convertDirect(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal amount
    ) {

        Optional<ExchangeRate> exchangeRateOptional =
                exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRateOptional.isPresent()) {

            ExchangeRate exchangeRate = exchangeRateOptional.get();
            Currency baseCurrency = exchangeRate.getBaseCurrency();
            Currency targetCurrency = exchangeRate.getTargetCurrency();
            BigDecimal rate = exchangeRate.getRate();
            BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);

            return Optional.of(new ExchangeResponse(baseCurrency, targetCurrency, rate, amount, convertedAmount));
        }

        return Optional.empty();
    }

    private Optional<ExchangeResponse> convertReverse(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal amount
    ) {

        String tmpCurrencyCode = baseCurrencyCode;
        baseCurrencyCode = targetCurrencyCode;
        targetCurrencyCode = tmpCurrencyCode;

        Optional<ExchangeRate> exchangeRateOptional =
                exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRateOptional.isPresent()) {

            ExchangeRate exchangeRate = exchangeRateOptional.get();
            Currency baseCurrency = exchangeRate.getBaseCurrency();
            Currency targetCurrency = exchangeRate.getTargetCurrency();
            BigDecimal rate = exchangeRate.getRate();
            rate = BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_UP);
            BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);

            return Optional.of(new ExchangeResponse(baseCurrency, targetCurrency, rate, amount, convertedAmount));
        }

        return Optional.empty();
    }

    public static final String USD_CODE = "USD";

    private Optional<ExchangeResponse> convertViaUsd(
            String baseCurrencyCode,
            String targetCurrencyCode,
            BigDecimal amount
    ) {

        Optional<ExchangeRate> exchangeRateUsdToBaseCurrency =
                exchangeRateRepository.findByCodes(USD_CODE, baseCurrencyCode);
        Optional<ExchangeRate> exchangeRateUsdToTargetCurrency =
                exchangeRateRepository.findByCodes(USD_CODE, targetCurrencyCode);

        if (exchangeRateUsdToBaseCurrency.isPresent() && exchangeRateUsdToTargetCurrency.isPresent()) {

            BigDecimal usdToBaseCurrency = exchangeRateUsdToBaseCurrency.get().getRate();
            BigDecimal baseCurrencyToUsd =
                    BigDecimal.ONE.divide(usdToBaseCurrency, 6, RoundingMode.HALF_UP);
            BigDecimal usdToTargetCurrency = exchangeRateUsdToTargetCurrency.get().getRate();
            BigDecimal rate =
                    baseCurrencyToUsd.multiply(usdToTargetCurrency);

            Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
            Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);
            if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {

                Currency baseCurrency = baseCurrencyOptional.get();
                Currency targetCurrency = targetCurrencyOptional.get();
                BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);

                return Optional.of(new ExchangeResponse(baseCurrency, targetCurrency, rate, amount, convertedAmount));
            }
        }

        return Optional.empty();
    }
}
