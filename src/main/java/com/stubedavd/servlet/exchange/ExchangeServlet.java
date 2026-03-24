package com.stubedavd.servlet.exchange;

import com.stubedavd.service.ExchangeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.io.IOException;

import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.utils.Validator;
import com.stubedavd.model.response.ExchangeResponse;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {

    private ExchangeService exchangeService;

    @Override
    public void init() throws ServletException {

        super.init();

        exchangeService =
                (ExchangeService) getServletContext().getAttribute("exchangeService");

        if (exchangeService == null) {
            throw new NotFoundException("No exchange service found");
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

        ExchangeResponse exchangeResponse =
                exchangeService.getExchangeResponse(baseCurrencyCode, targetCurrencyCode, amount);

        sendJson(response, HttpServletResponse.SC_OK, exchangeResponse);
    }
}
