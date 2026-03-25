package com.stubedavd.dto.response;

import com.stubedavd.model.Currency;

import java.math.BigDecimal;

public record ExchangeResponseDto(
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
}
