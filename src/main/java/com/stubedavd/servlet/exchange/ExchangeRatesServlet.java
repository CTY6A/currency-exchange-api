package com.stubedavd.servlet.exchange;

import com.stubedavd.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.io.IOException;

import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.utils.Validator;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {

        super.init();

        exchangeRateService =
                (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");

        if (exchangeRateService == null) {
            throw new NotFoundException("No exchange rate service found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<ExchangeRate> exchangeRates = exchangeRateService.findAll();
        sendJson(response, HttpServletResponse.SC_OK, exchangeRates);
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

        ExchangeRate exchangeRate = exchangeRateService.save(baseCurrencyCode, targetCurrencyCode, rate);
        sendJson(response, HttpServletResponse.SC_CREATED, exchangeRate);
    }
}
