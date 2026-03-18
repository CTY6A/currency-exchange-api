package com.stubedavd.servlets;

import com.stubedavd.DAO.CurrencyDAO;
import com.stubedavd.DAO.ExchangeRateDAO;
import com.stubedavd.DTO.ResponseHelper;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ErrorResponse;
import com.stubedavd.models.ExchangeRate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final int ZERO_ID = 1;
    public static final int TWO_CODES_AND_SLASH = 7;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() != TWO_CODES_AND_SLASH) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse error = new ErrorResponse("A required form field is missing");
            new ResponseHelper(resp, error);
        } else {
            String exchangeRateCodes = pathInfo.substring(1);
            String baseCurrencyCode = exchangeRateCodes.substring(0, 3).toUpperCase();
            String targetCurrencyCode = exchangeRateCodes.substring(3).toUpperCase();
            try {
                ExchangeRateDAO dao = new ExchangeRateDAO();
                ExchangeRate exchangeRate = dao.findByPair(baseCurrencyCode, targetCurrencyCode);
                if (exchangeRate == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    ErrorResponse error = new ErrorResponse("No exchange rate found for " + baseCurrencyCode + " " + targetCurrencyCode);
                    new ResponseHelper(resp, error);
                } else {
                    new ResponseHelper(resp, exchangeRate);
                }
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() != TWO_CODES_AND_SLASH) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse error = new ErrorResponse("A required form field is missing");
            new ResponseHelper(resp, error);
        } else {
            String exchangeRateCodes = pathInfo.substring(1);
            String baseCurrencyCode = exchangeRateCodes.substring(0, 3).toUpperCase();
            String targetCurrencyCode = exchangeRateCodes.substring(3).toUpperCase();
            String rateString = req.getParameter("rate");
            if (isParametersValid(baseCurrencyCode, targetCurrencyCode, rateString)) {
                try {
                    baseCurrencyCode = baseCurrencyCode.toUpperCase();
                    targetCurrencyCode = targetCurrencyCode.toUpperCase();
                    BigDecimal rate = new BigDecimal(rateString);

                    ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
                    ExchangeRate exchangeRate = exchangeRateDAO.findByPair(baseCurrencyCode, targetCurrencyCode);
                    if (exchangeRate != null) {
                        CurrencyDAO currencyDAO = new CurrencyDAO();
                        Currency baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
                        Currency targetCurrency = currencyDAO.findByCode(targetCurrencyCode);
                        if (baseCurrency != null && targetCurrency != null) {
                            exchangeRate = new ExchangeRate(ZERO_ID, baseCurrency, targetCurrency, rate);
                            ExchangeRate resultCurrency = exchangeRateDAO.update(exchangeRate);
                            if (resultCurrency == null) {
                                throw new IOException("Exchange rate could not be saved");
                            }
                            new ResponseHelper(resp, resultCurrency);
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            ErrorResponse error = new ErrorResponse("Currency could not be found");
                            new ResponseHelper(resp, error);
                        }
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        ErrorResponse error = new ErrorResponse("Exchange rate could not be found");
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
