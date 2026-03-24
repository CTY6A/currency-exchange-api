package com.stubedavd.service;

import com.stubedavd.model.response.ExchangeResponse;

import java.math.BigDecimal;

public interface ExchangeService {

    ExchangeResponse getExchangeResponse(String currencyCodeFrom, String currencyCodeTo, BigDecimal amount);
}
