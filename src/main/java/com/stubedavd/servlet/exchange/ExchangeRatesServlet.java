package com.stubedavd.servlet.exchange;

import com.stubedavd.dto.request.ExchangeRateRequestDto;
import com.stubedavd.dto.response.ExchangeRateResponseDto;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.listener.ContextListener;
import com.stubedavd.mapper.ExchangeRateMapper;
import com.stubedavd.service.ExchangeRateService;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateMapper exchangeRateMapper;

    @Override
    public void init() throws ServletException {

        super.init();

        exchangeRateService =
                (ExchangeRateService) getServletContext().getAttribute(ContextListener.EXCHANGE_RATE_SERVICE);

        if (exchangeRateService == null) {
            throw new NotFoundException("No exchange rate service found");
        }

        this.exchangeRateMapper =
                (ExchangeRateMapper) getServletContext().getAttribute(ContextListener.EXCHANGE_RATE_MAPPER);

        if (exchangeRateMapper == null) {
            throw new NotFoundException("Exchange rate mapper not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<ExchangeRateResponseDto> exchangeRatesResponseDto = exchangeRateService.getAll();

        sendJson(response, HttpServletResponse.SC_OK, exchangeRatesResponseDto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rateString = request.getParameter("rate");

        Validator.validateExchangeRate(baseCurrencyCode, targetCurrencyCode, rateString);

        baseCurrencyCode = baseCurrencyCode.toUpperCase();
        targetCurrencyCode = targetCurrencyCode.toUpperCase();
        BigDecimal rate = new BigDecimal(rateString);

        ExchangeRateRequestDto exchangeRateRequestDto =
                exchangeRateMapper.toRequestDto(baseCurrencyCode, targetCurrencyCode, rate);

        ExchangeRateResponseDto exchangeRateResponseDto =
                exchangeRateService.save(exchangeRateRequestDto);

        sendJson(response, HttpServletResponse.SC_CREATED, exchangeRateResponseDto);
    }
}
