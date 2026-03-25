package com.stubedavd.mapper;

import com.stubedavd.dto.request.ExchangeRateRequestDto;
import com.stubedavd.dto.response.ExchangeRateResponseDto;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper
public interface ExchangeRateMapper {

    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    ExchangeRateRequestDto toRequestDto(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);

    @Mapping(target = "id", ignore = true)
    ExchangeRate toModel(Currency baseCurrency, Currency targetCurrency, BigDecimal rate);

    ExchangeRateResponseDto toResponseDto(ExchangeRate exchangeRate);
}
