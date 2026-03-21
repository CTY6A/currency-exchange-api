package com.stubedavd.servlet.exchange;

import com.stubedavd.model.response.ExchangeResponse;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.repository.JdbcExchangeRateRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.ResponseHelper;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.service.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Optional;

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

                JdbcExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();
                Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
                if (exchangeRate.isEmpty()) {
                    JdbcCurrencyRepository currencyRepository = new JdbcCurrencyRepository();
                    Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
                    Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);
                    if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {
                        exchangeRate = Optional.of(new ExchangeRate(ZERO_ID, baseCurrencyOptional.get(), targetCurrencyOptional.get(), amount));
                        ExchangeService exchangeService = new ExchangeService();
                        ExchangeRate resultExchangeRate = exchangeService.findExchangeRate(exchangeRate.orElse(null));
                        if (resultExchangeRate == null) {
                            throw new InfrastructureException();
                        }
                        ExchangeResponse exchangeResponse = new ExchangeResponse(resultExchangeRate, amount);
                        new ResponseHelper(resp, exchangeResponse);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        ErrorResponse error = new ErrorResponse("Currency could not be found");
                        new ResponseHelper(resp, error);
                    }
                } else {
                    ExchangeResponse exchangeResponse = new ExchangeResponse(exchangeRate.orElse(null), amount);
                    new ResponseHelper(resp, exchangeResponse);
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
