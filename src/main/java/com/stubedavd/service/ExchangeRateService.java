package com.stubedavd.service;

import com.stubedavd.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {

    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode);

    ExchangeRate save(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);

    ExchangeRate update(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);
}
