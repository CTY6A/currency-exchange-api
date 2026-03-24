package com.stubedavd.servlet.exchange;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Optional;
import java.io.IOException;

import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.utils.Validator;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {

    private ExchangeRateRepository repository;

    @Override
    public void init() throws ServletException {
        super.init();

        repository =
                (ExchangeRateRepository) getServletContext().getAttribute("exchangeRateRepository");

        if (repository == null) {
            throw new NotFoundException("No exchange rate repository found");
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

        Optional<ExchangeRate> exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isPresent()) {
            sendJson(response, HttpServletResponse.SC_OK, exchangeRate.get());
        } else {
            throw new NotFoundException("No exchange rate found for " + baseCurrencyCode + " " + targetCurrencyCode);
        }
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

        Optional<ExchangeRate> exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isPresent()) {

            CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
            Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
            Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);

            if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {

                exchangeRate = Optional.of(new ExchangeRate(baseCurrencyOptional.get(), targetCurrencyOptional.get(), rate));
                ExchangeRate resultCurrency = repository.update(exchangeRate.orElse(null));

                if (resultCurrency == null) {
                    throw new InfrastructureException("Database is available");
                }

                sendJson(response, HttpServletResponse.SC_OK, resultCurrency);
            } else {
                throw new NotFoundException("Currency could not be found");
            }
        } else {
            throw new NotFoundException("Exchange rate could not be found");
        }
    }
}
