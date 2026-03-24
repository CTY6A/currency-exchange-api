package com.stubedavd.service;

import com.stubedavd.exception.NotFoundException;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    public ExchangeRateServiceImpl(
            ExchangeRateRepository exchangeRateRepository,
            CurrencyRepository currencyRepository
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<ExchangeRate> findAll() {
        return exchangeRateRepository.findAll();
    }

    @Override
    public Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        return exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
    }

    @Override
    public ExchangeRate save(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
        Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);

        if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {

            ExchangeRate exchangeRate =
                    new ExchangeRate(baseCurrencyOptional.get(), targetCurrencyOptional.get(), rate);
            return exchangeRateRepository.save(exchangeRate);
        } else {
            throw new NotFoundException("Currency could not be found");
        }
    }

    @Override
    public ExchangeRate update(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {

        Optional<ExchangeRate> exchangeRate =
                exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isPresent()) {
            return exchangeRateRepository.update(exchangeRate.get());
        } else {
            throw new NotFoundException("Exchange rate could not be found");
        }
    }
}
