package com.stubedavd.service;

import com.stubedavd.dto.request.ExchangeRateRequestDto;
import com.stubedavd.dto.response.ExchangeRateResponseDto;

import java.util.List;

public interface ExchangeRateService {

    List<ExchangeRateResponseDto> getAll();

    ExchangeRateResponseDto findByCodes(String baseCurrencyCode, String targetCurrencyCode);

    ExchangeRateResponseDto save(ExchangeRateRequestDto exchangeRateRequestDto);

    ExchangeRateResponseDto update(ExchangeRateRequestDto exchangeRateRequestDto);
}
