package com.stubedavd.dto.response;

import com.stubedavd.model.Currency;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(Integer id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
}
