package com.stubedavd.servlet.exchange;

import com.stubedavd.dto.request.ExchangeRequestDto;
import com.stubedavd.dto.response.ExchangeResponseDto;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.listener.ContextListener;
import com.stubedavd.mapper.ExchangeMapper;
import com.stubedavd.service.ExchangeService;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {

    private ExchangeService exchangeService;
    private ExchangeMapper exchangeMapper;

    @Override
    public void init() throws ServletException {

        super.init();

        exchangeService =
                (ExchangeService) getServletContext().getAttribute(ContextListener.EXCHANGE_SERVICE);

        if (exchangeService == null) {
            throw new NotFoundException("No exchange service found");
        }

        exchangeMapper =
                (ExchangeMapper) getServletContext().getAttribute(ContextListener.EXCHANGE_MAPPER);

        if (exchangeMapper == null) {
            throw new NotFoundException("No exchange mapper found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String baseCurrencyCode = request.getParameter("from");
        String targetCurrencyCode = request.getParameter("to");
        String amountString = request.getParameter("amount");

        Validator.validateExchangeRate(baseCurrencyCode, targetCurrencyCode, amountString);

        baseCurrencyCode = baseCurrencyCode.toUpperCase();
        targetCurrencyCode = targetCurrencyCode.toUpperCase();
        BigDecimal amount = new BigDecimal(amountString);

        ExchangeRequestDto exchangeRequestDto =
                exchangeMapper.toRequestDto(baseCurrencyCode, targetCurrencyCode, amount);

        ExchangeResponseDto exchangeResponseDto =
                exchangeService.convertCurrency(exchangeRequestDto);

        sendJson(response, HttpServletResponse.SC_OK, exchangeResponseDto);
    }
}
