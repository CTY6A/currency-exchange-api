package com.stubedavd.service;

import com.stubedavd.dto.request.ExchangeRequestDto;
import com.stubedavd.dto.response.ExchangeResponseDto;

public interface ExchangeService {

    ExchangeResponseDto convertCurrency(ExchangeRequestDto exchangeRequestDto);
}
