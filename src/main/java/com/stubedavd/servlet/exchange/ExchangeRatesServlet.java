package com.stubedavd.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.ValidationException;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
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
        try {
            List<ExchangeRate> exchangeRates = repository.findAll();
            mapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (InfrastructureException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Database is unavailable"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateString = req.getParameter("rate");
        try {
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
                    throw new InfrastructureException();
                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                mapper.writeValue(resp.getWriter(), resultExchangeRate);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(
                        "Currency could not be found"
                ));
            }
        } catch (InfrastructureException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Database is unavailable"
            ));
        } catch (AlreadyExistException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Exchange rate already exist"
            ));
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Invalid parameters"
            ));
        }
    }
}
