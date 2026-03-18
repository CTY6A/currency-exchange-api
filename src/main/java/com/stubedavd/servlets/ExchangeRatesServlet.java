package com.stubedavd.servlets;

import com.stubedavd.DAO.CurrencyDAO;
import com.stubedavd.DAO.ExchangeRateDAO;
import com.stubedavd.DTO.ResponseHelper;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ErrorResponse;
import com.stubedavd.models.ExchangeRate;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final int ZERO_ID = 1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            ExchangeRateDAO dao = new ExchangeRateDAO();
            List<ExchangeRate> exchangeRates = dao.findAll();
            new ResponseHelper(resp, exchangeRates);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse error = new ErrorResponse("Database is unavailable");
            new ResponseHelper(resp, error);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateString = req.getParameter("rate");
        if (isParametersValid(baseCurrencyCode, targetCurrencyCode, rateString)) {
            try {
                baseCurrencyCode = baseCurrencyCode.toUpperCase();
                targetCurrencyCode = targetCurrencyCode.toUpperCase();
                BigDecimal rate = new BigDecimal(rateString);

                ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
                ExchangeRate exchangeRate = exchangeRateDAO.findByPair(baseCurrencyCode, targetCurrencyCode);
                if (exchangeRate == null) {
                    CurrencyDAO currencyDAO = new CurrencyDAO();
                    Currency baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
                    Currency targetCurrency = currencyDAO.findByCode(targetCurrencyCode);
                    if (baseCurrency != null && targetCurrency != null) {
                        exchangeRate = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, rate);
                        ExchangeRate resultExchangeRate = exchangeRateDAO.save(exchangeRate);
                        if (resultExchangeRate == null) {
                            throw new IOException("Exchange rate could not be saved");
                        }
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        new ResponseHelper(resp, resultExchangeRate);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        ErrorResponse error = new ErrorResponse("Currency could not be found");
                        new ResponseHelper(resp, error);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    ErrorResponse error = new ErrorResponse("Exchange rate already exists");
                    new ResponseHelper(resp, error);
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

    private boolean isParametersValid(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        if (baseCurrencyCode == null || !baseCurrencyCode.matches("[A-z]{3}")) {
            return false;
        }

        if (targetCurrencyCode == null || !targetCurrencyCode.matches("[A-z]{3}")) {
            return false;
        }

        try {
            new BigDecimal(rate);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
