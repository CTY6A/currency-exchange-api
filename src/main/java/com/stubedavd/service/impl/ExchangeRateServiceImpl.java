package com.stubedavd.service.impl;

import com.stubedavd.dto.request.ExchangeRateRequestDto;
import com.stubedavd.dto.response.ExchangeRateResponseDto;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.mapper.ExchangeRateMapper;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.service.ExchangeRateService;

import java.util.List;

public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateServiceImpl(
            ExchangeRateRepository exchangeRateRepository,
            CurrencyRepository currencyRepository,
            ExchangeRateMapper exchangeRateMapper
    ) {

        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Override
    public List<ExchangeRateResponseDto> getAll() {

        List<ExchangeRate> exchangeRates = exchangeRateRepository.findAll();

        return exchangeRates.stream().map(exchangeRateMapper::toResponseDto).toList();
    }

    @Override
    public ExchangeRateResponseDto findByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        ExchangeRate exchangeRate = exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode)
                .orElseThrow(() -> new NotFoundException(
                        "No exchange rate found for code " + baseCurrencyCode + " and target " + targetCurrencyCode
                ));

        return exchangeRateMapper.toResponseDto(exchangeRate);
    }

    @Override
    public ExchangeRateResponseDto save(ExchangeRateRequestDto exchangeRateRequestDto) {

        Currency baseCurrency = currencyRepository.findByCode(exchangeRateRequestDto.baseCurrencyCode())
                .orElseThrow(() -> new NotFoundException(
                        "No exchange rate found for code " + exchangeRateRequestDto.baseCurrencyCode()
                ));

        Currency targetCurrency = currencyRepository.findByCode(exchangeRateRequestDto.targetCurrencyCode())
                .orElseThrow(() -> new NotFoundException(
                        "No exchange rate found for code " + exchangeRateRequestDto.targetCurrencyCode()
                ));

        ExchangeRate exchangeRate =
                exchangeRateMapper.toModel(baseCurrency, targetCurrency, exchangeRateRequestDto.rate());

        exchangeRate = exchangeRateRepository.save(exchangeRate);

        return exchangeRateMapper.toResponseDto(exchangeRate);
    }

    @Override
    public ExchangeRateResponseDto update(ExchangeRateRequestDto exchangeRateRequestDto) {

        ExchangeRate exchangeRate =
                exchangeRateRepository.findByCodes(
                            exchangeRateRequestDto.baseCurrencyCode(),
                            exchangeRateRequestDto.targetCurrencyCode()
                        )
                .orElseThrow(() -> new NotFoundException("Exchange rate could not be found"));

        exchangeRate = new ExchangeRate(
                exchangeRate.getId(),
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRateRequestDto.rate()
        );

        exchangeRate = exchangeRateRepository.update(exchangeRate);

        return exchangeRateMapper.toResponseDto(exchangeRate);
    }
}
