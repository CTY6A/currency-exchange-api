package com.stubedavd.servlet.exchange;

import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.repository.JdbcExchangeRateRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.ResponseHelper;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.model.ExchangeRate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final int ZERO_ID = 1;
    public static final int TWO_CODES_AND_SLASH = 7;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

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
                JdbcExchangeRateRepository dao = new JdbcExchangeRateRepository();
                Optional<ExchangeRate> exchangeRate = dao.findByCodes(baseCurrencyCode, targetCurrencyCode);
                if (exchangeRate.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    ErrorResponse error = new ErrorResponse("No exchange rate found for " + baseCurrencyCode + " " + targetCurrencyCode);
                    new ResponseHelper(resp, error);
                } else {
                    new ResponseHelper(resp, exchangeRate);
                }
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            }
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

                    JdbcExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();
                    Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
                    if (exchangeRate.isPresent()) {
                        JdbcCurrencyRepository currencyRepository = new JdbcCurrencyRepository();
                        Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
                        Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);
                        if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {
                            exchangeRate = Optional.of(new ExchangeRate(ZERO_ID, baseCurrencyOptional.get(), targetCurrencyOptional.get(), rate));
                            ExchangeRate resultCurrency = exchangeRateRepository.update(exchangeRate.orElse(null));
                            if (resultCurrency == null) {
                                throw new InfrastructureException();
                            }
                            new ResponseHelper(resp, resultCurrency);
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            ErrorResponse error = new ErrorResponse("Currency could not be found");
                            new ResponseHelper(resp, error);
                        }
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        ErrorResponse error = new ErrorResponse("ExchangeResponse rate could not be found");
                        new ResponseHelper(resp, error);
                    }
                } catch (InfrastructureException e) {
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
