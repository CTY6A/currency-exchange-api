package com.stubedavd.model.response;

import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;

import java.math.BigDecimal;

public class ExchangeResponse {
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private final BigDecimal rate;
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;

    public ExchangeResponse(ExchangeRate exchangeRate, BigDecimal amount) {
        this.baseCurrency = exchangeRate.getBaseCurrency();
        this.targetCurrency = exchangeRate.getTargetCurrency();
        this.rate = exchangeRate.getRate();
        this.amount = amount;
        this.convertedAmount = amount.multiply(rate);
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    @Override
    public String toString() {
        return "ExchangeResponse{" +
                "baseCurrency=" + baseCurrency +
                ", targetCurrency=" + targetCurrency +
                ", rate=" + rate +
                ", amount=" + amount +
                ", convertedAmount=" + convertedAmount +
                '}';
    }
}
