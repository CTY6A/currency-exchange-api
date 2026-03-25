package com.stubedavd.dto;

import com.stubedavd.model.Currency;

import java.math.BigDecimal;

public record ExchangeRateDto(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
}
