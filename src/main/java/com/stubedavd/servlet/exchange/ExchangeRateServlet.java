package com.stubedavd.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final int ZERO_ID = 1;
    public static final int TWO_CODES_AND_SLASH = 7;

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
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() != TWO_CODES_AND_SLASH) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "A required form field is missing"
            ));
        } else {
            String exchangeRateCodes = pathInfo.substring(1);
            String baseCurrencyCode = exchangeRateCodes.substring(0, 3).toUpperCase();
            String targetCurrencyCode = exchangeRateCodes.substring(3).toUpperCase();
            try {
                Optional<ExchangeRate> exchangeRate = repository.findByCodes(baseCurrencyCode, targetCurrencyCode);
                if (exchangeRate.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse(
                            "No exchange rate found for " + baseCurrencyCode + " " + targetCurrencyCode
                    ));
                } else {
                    mapper.writeValue(resp.getWriter(), exchangeRate.get());
                }
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(
                        "Database is unavailable"
                ));
            }
        }
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() != TWO_CODES_AND_SLASH) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "A required form field is missing"
            ));
        } else {
            String exchangeRateCodes = pathInfo.substring(1);
            String baseCurrencyCode = exchangeRateCodes.substring(0, 3).toUpperCase();
            String targetCurrencyCode = exchangeRateCodes.substring(3).toUpperCase();
            String rateString = req.getParameter("rate");
                try {
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
                            exchangeRate = Optional.of(new ExchangeRate(ZERO_ID, baseCurrencyOptional.get(), targetCurrencyOptional.get(), rate));
                            ExchangeRate resultCurrency = repository.update(exchangeRate.orElse(null));
                            if (resultCurrency == null) {
                                throw new InfrastructureException();
                            }
                            mapper.writeValue(resp.getWriter(), resultCurrency);
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                                    "Currency could not be found"
                            ));
                        }
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        mapper.writeValue(resp.getWriter(), new ErrorResponse(
                                "ExchangeResponse rate could not be found"
                        ));
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
}
