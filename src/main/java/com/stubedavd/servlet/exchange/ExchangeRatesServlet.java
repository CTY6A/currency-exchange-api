package com.stubedavd.servlet.exchange;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.utils.Validator;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {

    private static final int ZERO_ID = 1;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<ExchangeRate> exchangeRates = repository.findAll();
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

        CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
        Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
        Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);

        if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {

            ExchangeRate exchangeRate = new ExchangeRate(ZERO_ID, baseCurrencyOptional.get(), targetCurrencyOptional.get(), rate);
            ExchangeRate resultExchangeRate = repository.save(exchangeRate);

            if (resultExchangeRate == null) {
                throw new InfrastructureException("Database is available");
            }

            sendJson(response, HttpServletResponse.SC_CREATED, resultExchangeRate);
        } else {
            throw new NotFoundException("Currency could not be found");
        }
    }
}
