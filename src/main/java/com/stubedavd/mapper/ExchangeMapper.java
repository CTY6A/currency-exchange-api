package com.stubedavd.mapper;

import com.stubedavd.dto.ExchangeRateDto;
import com.stubedavd.dto.request.ExchangeRequestDto;
import com.stubedavd.dto.response.ExchangeResponseDto;
import com.stubedavd.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper
public interface ExchangeMapper {

    ExchangeMapper INSTANCE = Mappers.getMapper(ExchangeMapper.class);

    ExchangeRequestDto toRequestDto(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount);

    ExchangeRateDto toExchangeRateDto(ExchangeRate exchangeRate);

    ExchangeResponseDto toResponseDto(
            ExchangeRateDto exchangeRateDto,
            BigDecimal amount,
            BigDecimal convertedAmount
    );
}
