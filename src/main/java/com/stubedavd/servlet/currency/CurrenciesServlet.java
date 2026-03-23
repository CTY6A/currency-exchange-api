package com.stubedavd.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.ValidationException;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import com.stubedavd.utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private static final int ZERO_ID = 0;
    private final ObjectMapper mapper = new ObjectMapper();

    private CurrencyRepository repository;

    @Override
    public void init() throws ServletException {
        super.init();

        this.repository =
                (CurrencyRepository) getServletContext().getAttribute("currencyRepository");

        if (repository == null) {
            throw new IllegalStateException("Currency repository not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        try {
            List<Currency> currencies = repository.findAll();
            mapper.writeValue(resp.getWriter(), currencies);
        } catch (InfrastructureException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "Database is unavailable"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
            try {
                Validator.validateCurrency(name, code, sign);
                Currency currency = new Currency(ZERO_ID, name, code.toUpperCase(), sign);
                Currency resultCurrency = repository.save(currency);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                mapper.writeValue(resp.getWriter(), resultCurrency);
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(
                        "Database is unavailable"
                ));
            } catch (AlreadyExistException e) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(
                        "Currency already exists"
                ));
            } catch (ValidationException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(
                        "Invalid parameters"
                ));

        }
    }
}
