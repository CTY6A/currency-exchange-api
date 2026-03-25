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

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {

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
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getMethod().equals("PATCH")) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        Validator.validateTwoCodesPath(pathInfo);

        String exchangeRateCodes = pathInfo.substring(1);
        String baseCurrencyCode = exchangeRateCodes.substring(0, 3).toUpperCase();
        String targetCurrencyCode = exchangeRateCodes.substring(3).toUpperCase();

        ExchangeRateResponseDto exchangeRateResponseDto =
                exchangeRateService.findByCodes(baseCurrencyCode, targetCurrencyCode);

        sendJson(response, HttpServletResponse.SC_OK, exchangeRateResponseDto);
    }

    private void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        Validator.validateTwoCodesPath(pathInfo);

        String exchangeRateCodes = pathInfo.substring(1);
        String baseCurrencyCode = exchangeRateCodes.substring(0, 3).toUpperCase();
        String targetCurrencyCode = exchangeRateCodes.substring(3).toUpperCase();

        String rateParameter = request.getReader().readLine();

        Validator.validateRateParameter(rateParameter);

        String rateString = rateParameter.replace("rate=", "");

        Validator.validateExchangeRate(baseCurrencyCode, targetCurrencyCode, rateString);

        baseCurrencyCode = baseCurrencyCode.toUpperCase();
        targetCurrencyCode = targetCurrencyCode.toUpperCase();
        BigDecimal rate = new BigDecimal(rateString);

        ExchangeRateRequestDto exchangeRateRequestDto =
                exchangeRateMapper.toRequestDto(baseCurrencyCode, targetCurrencyCode, rate);

        ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.update(exchangeRateRequestDto);

        sendJson(response, HttpServletResponse.SC_OK, exchangeRateResponseDto);
    }
}
