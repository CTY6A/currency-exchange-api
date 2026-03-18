package com.stubedavd.servlets;

import com.stubedavd.DAO.CurrencyDAO;
import com.stubedavd.DAO.ExchangeRateDAO;
import com.stubedavd.DTO.ResponseHelper;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ErrorResponse;
import com.stubedavd.models.Exchange;
import com.stubedavd.models.ExchangeRate;
import com.stubedavd.services.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private static final int ZERO_ID = 1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountString = req.getParameter("amount");
        if (isParametersValid(baseCurrencyCode, targetCurrencyCode, amountString)) {
            try {
                baseCurrencyCode = baseCurrencyCode.toUpperCase();
                targetCurrencyCode = targetCurrencyCode.toUpperCase();
                BigDecimal amount = new BigDecimal(amountString);

                ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
                ExchangeRate exchangeRate = exchangeRateDAO.findByPair(baseCurrencyCode, targetCurrencyCode);
                if (exchangeRate == null) {
                    CurrencyDAO currencyDAO = new CurrencyDAO();
                    Currency baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
                    Currency targetCurrency = currencyDAO.findByCode(targetCurrencyCode);
                    if (baseCurrency != null && targetCurrency != null) {
                        exchangeRate = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, amount);
                        ExchangeService exchangeService = new ExchangeService();
                        ExchangeRate resultExchangeRate = exchangeService.findExchangeRate(exchangeRate);
                        if (resultExchangeRate == null) {
                            throw new IOException("Exchange rate could not be saved");
                        }
                        Exchange exchange = new Exchange(resultExchangeRate, amount);
                        new ResponseHelper(resp, exchange);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        ErrorResponse error = new ErrorResponse("Currency could not be found");
                        new ResponseHelper(resp, error);
                    }
                } else {
                    Exchange exchange = new Exchange(exchangeRate, amount);
                    new ResponseHelper(resp, exchange);
                }
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse error = new ErrorResponse("Invalid parameters");
            new ResponseHelper(resp, error);
        }
    }

    private boolean isParametersValid(String baseCurrencyCode, String targetCurrencyCode, String amount) {
        if (baseCurrencyCode == null || !baseCurrencyCode.matches("[A-z]{3}")) {
            return false;
        }

        if (targetCurrencyCode == null || !targetCurrencyCode.matches("[A-z]{3}")) {
            return false;
        }

        try {
            new BigDecimal(amount);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
