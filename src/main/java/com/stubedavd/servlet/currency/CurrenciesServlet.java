package com.stubedavd.servlet.currency;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.util.List;
import java.io.IOException;

import com.stubedavd.exception.NotFoundException;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.model.Currency;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.utils.Validator;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {

    private static final int ZERO_ID = 0;

    private CurrencyRepository repository;

    @Override
    public void init() throws ServletException {

        super.init();

        this.repository =
                (CurrencyRepository) getServletContext().getAttribute("currencyRepository");

        if (repository == null) {
            throw new NotFoundException("Currency repository not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<Currency> currencies = repository.findAll();
        sendJson(response, HttpServletResponse.SC_OK, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        Validator.validateCurrency(name, code, sign);

        Currency currency = new Currency(ZERO_ID, name, code.toUpperCase(), sign);
        Currency resultCurrency = repository.save(currency);

        sendJson(response, HttpServletResponse.SC_CREATED, resultCurrency);
    }
}
