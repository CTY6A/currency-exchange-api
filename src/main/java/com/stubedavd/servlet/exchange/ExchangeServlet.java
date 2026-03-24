package com.stubedavd.servlet.exchange;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Optional;
import java.io.IOException;

import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.utils.Validator;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ExchangeResponse;
import com.stubedavd.service.ExchangeService;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {

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

        String baseCurrencyCode = request.getParameter("from");
        String targetCurrencyCode = request.getParameter("to");
        String amountString = request.getParameter("amount");

        Validator.validateExchangeRate(baseCurrencyCode, targetCurrencyCode, amountString);

        baseCurrencyCode = baseCurrencyCode.toUpperCase();
        targetCurrencyCode = targetCurrencyCode.toUpperCase();
        BigDecimal amount = new BigDecimal(amountString);

        Optional<ExchangeRate> exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isPresent()) {

            ExchangeResponse exchangeResponse = new ExchangeResponse(exchangeRate.orElse(null), amount);

            sendJson(response, HttpServletResponse.SC_OK, exchangeResponse);
        } else {

            CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
            Optional<Currency> baseCurrencyOptional = currencyRepository.findByCode(baseCurrencyCode);
            Optional<Currency> targetCurrencyOptional = currencyRepository.findByCode(targetCurrencyCode);

            if (baseCurrencyOptional.isPresent() && targetCurrencyOptional.isPresent()) {

                exchangeRate = Optional.of(new ExchangeRate(ZERO_ID, baseCurrencyOptional.get(), targetCurrencyOptional.get(), amount));
                ExchangeService exchangeService = new ExchangeService();
                ExchangeRate resultExchangeRate = exchangeService.findExchangeRate(exchangeRate.orElse(null));

                if (resultExchangeRate == null) {
                    throw new InfrastructureException("Database is available");
                }

                ExchangeResponse exchangeResponse = new ExchangeResponse(resultExchangeRate, amount);
                sendJson(response, HttpServletResponse.SC_OK, exchangeResponse);
            } else {
                throw new NotFoundException("Currency could not be found");
            }
        }
    }
}
