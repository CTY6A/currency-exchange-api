package com.stubedavd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.stubedavd.dto.request.CurrencyRequestDto;
import com.stubedavd.dto.response.CurrencyResponseDto;
import com.stubedavd.model.Currency;

@Mapper
public interface CurrencyMapper {

    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    CurrencyRequestDto toRequestDto(String name, String code, String sign);

    @Mapping(target = "id", ignore = true)
    Currency toModel(CurrencyRequestDto currencyRequestDto);

    CurrencyResponseDto toResponseDto(Currency currency);
}
