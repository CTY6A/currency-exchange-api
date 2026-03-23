package com.stubedavd.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.exception.ValidationException;
import com.stubedavd.model.response.ExchangeResponse;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.service.ExchangeService;
import com.stubedavd.utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private static final int ZERO_ID = 1;

    private final ObjectMapper mapper = new ObjectMapper();

    private ExchangeRateRepository repository;

    @Override
    public void init() throws ServletException {
        super.init();

        repository =
                (ExchangeRateRepository) getServletContext().getAttribute("exchangeRateRepository");

        if (repository == null) {
            throw new IllegalStateException("No exchange rate repository found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountString = req.getParameter("amount");
        try {
            Validator.validateExchangeRate(baseCurrencyCode, targetCurrencyCode, amountString);
            baseCurrencyCode = baseCurrencyCode.toUpperCase();
            targetCurrencyCode = targetCurrencyCode.toUpperCase();
            BigDecimal amount = new BigDecimal(amountString);

            Optional<ExchangeRate> exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRate.isEmpty()) {
                CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
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
                    mapper.writeValue(resp.getWriter(), exchangeResponse);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse(
                            "Currency could not be found"
                    ));
                }
            } else {
                ExchangeResponse exchangeResponse = new ExchangeResponse(exchangeRate.orElse(null), amount);
                mapper.writeValue(resp.getWriter(), exchangeResponse);
            }
        } catch (InfrastructureException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Database is unavailable"
            ));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Invalid parameters"
            ));
        }
    }
}
