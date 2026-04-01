package com.stubedavd.service.impl;

import com.stubedavd.dto.ExchangeRateDto;
import com.stubedavd.dto.request.ExchangeRequestDto;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.dto.response.ExchangeResponseDto;
import com.stubedavd.mapper.ExchangeMapper;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.service.ExchangeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeServiceImpl implements ExchangeService {

    public static final String CROSS_CONVERT_CURRENCY = "USD";
    public static final int RATE_SCALE = 6;
    public static final int CONVERTED_AMOUNT_SCALE = 2;

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeMapper exchangeMapper;

    public ExchangeServiceImpl(
            ExchangeRateRepository exchangeRateRepository,
            ExchangeMapper exchangeMapper
    ) {

        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeMapper = exchangeMapper;
    }

    @Override
    public ExchangeResponseDto convertCurrency(ExchangeRequestDto exchangeRequestDto) {

        String baseCurrencyCode = exchangeRequestDto.baseCurrencyCode();
        String targetCurrencyCode = exchangeRequestDto.targetCurrencyCode();

        ExchangeRateDto exchangeRateDto = findDirectExchangeRate(baseCurrencyCode, targetCurrencyCode)
                .or(() -> findReverseExchangeRate(baseCurrencyCode, targetCurrencyCode))
                .or(() -> getCrossExchangeRate(baseCurrencyCode, targetCurrencyCode))
                .orElseThrow(() -> new NotFoundException(
                        "Exchange could not be found for codes " + baseCurrencyCode + ", " + targetCurrencyCode
                ));

        BigDecimal convertedAmount = exchangeRequestDto.amount()
                .multiply(exchangeRateDto.rate())
                .setScale(CONVERTED_AMOUNT_SCALE, RoundingMode.HALF_EVEN);

        return exchangeMapper.toResponseDto(
                exchangeRateDto,
                exchangeRequestDto.amount(),
                convertedAmount
        );
    }

    private Optional<ExchangeRateDto> findDirectExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {

        return exchangeRateRepository
                .findByCodes(baseCurrencyCode, targetCurrencyCode)
                .map(exchangeMapper::toExchangeRateDto);
    }

    private Optional<ExchangeRateDto> findReverseExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {

        return exchangeRateRepository.findByCodes(targetCurrencyCode, baseCurrencyCode)
                .map(exchangeRate -> {

                    BigDecimal rate = exchangeRate.rate();
                    rate = BigDecimal.ONE.divide(rate, RATE_SCALE, RoundingMode.HALF_UP);

                    return new ExchangeRateDto(
                            exchangeRate.targetCurrency(),
                            exchangeRate.baseCurrency(),
                            rate);
                });
    }

    private Optional<ExchangeRateDto> getCrossExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {

        Optional<ExchangeRate> exchangeRateUsdToBaseCurrency =
                exchangeRateRepository.findByCodes(CROSS_CONVERT_CURRENCY, baseCurrencyCode);

        Optional<ExchangeRate> exchangeRateUsdToTargetCurrency =
                exchangeRateRepository.findByCodes(CROSS_CONVERT_CURRENCY, targetCurrencyCode);

        if (exchangeRateUsdToBaseCurrency.isPresent() && exchangeRateUsdToTargetCurrency.isPresent()) {

            BigDecimal usdToBaseCurrency = exchangeRateUsdToBaseCurrency.get().rate();

            BigDecimal usdToTargetCurrency = exchangeRateUsdToTargetCurrency.get().rate();

            BigDecimal baseCurrencyToUsd =
                    BigDecimal.ONE.divide(usdToBaseCurrency, RATE_SCALE, RoundingMode.HALF_UP);

            BigDecimal rate =
                    baseCurrencyToUsd.multiply(usdToTargetCurrency);

            Currency baseCurrency = exchangeRateUsdToBaseCurrency.get().targetCurrency();
            Currency targetCurrency = exchangeRateUsdToTargetCurrency.get().targetCurrency();

            return Optional.of(new ExchangeRateDto(baseCurrency, targetCurrency, rate));
        }

        return Optional.empty();
    }
}
