package com.stubedavd.servlet.exchange;

import com.stubedavd.exception.AlreadyExistsException;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.ResponseHelper;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.model.ExchangeRate;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final int ZERO_ID = 1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            ExchangeRateRepository dao = new ExchangeRateRepository();
            List<ExchangeRate> exchangeRates = dao.findAll();
            new ResponseHelper(resp, exchangeRates);
        } catch (InfrastructureException e) {
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

                ExchangeRateRepository exchangeRateRepository = new ExchangeRateRepository();
                    CurrencyRepository currencyRepository = new CurrencyRepository();
                    Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
                    Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);
                    if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {
                        ExchangeRate exchangeRate = new ExchangeRate(ZERO_ID, baseCurrencyOptional.get(), targetCurrencyOptional.get(), rate);
                        ExchangeRate resultExchangeRate = exchangeRateRepository.save(exchangeRate);
                        if (resultExchangeRate == null) {
                            throw new InfrastructureException();
                        }
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        new ResponseHelper(resp, resultExchangeRate);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        ErrorResponse error = new ErrorResponse("Currency could not be found");
                        new ResponseHelper(resp, error);
                    }
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            } catch (AlreadyExistsException e) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                ErrorResponse error = new ErrorResponse("ExchangeResponse rate already exists");
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
