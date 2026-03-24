package com.stubedavd.servlet.exchange;

import com.stubedavd.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Optional;
import java.io.IOException;

import com.stubedavd.model.ExchangeRate;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.utils.Validator;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        super.init();

        exchangeRateService =
                (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");

        if (exchangeRateService == null) {
            throw new NotFoundException("No exchange rate service found");
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

        Optional<ExchangeRate> exchangeRate = exchangeRateService.findByCodes(baseCurrencyCode, targetCurrencyCode);

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

        ExchangeRate exchangeRate = exchangeRateService.update(baseCurrencyCode, targetCurrencyCode, rate);

        sendJson(response, HttpServletResponse.SC_OK, exchangeRate);
    }
}
