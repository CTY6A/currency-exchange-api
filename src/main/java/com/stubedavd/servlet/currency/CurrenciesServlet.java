package com.stubedavd.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.exception.AlreadyExistsException;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.ResponseHelper;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private static final int ZERO_ID = 0;
    private static final JdbcCurrencyRepository repository = new JdbcCurrencyRepository();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        if (isParametersValid(name, code, sign)) {
            try {
                JdbcCurrencyRepository repository = new JdbcCurrencyRepository();
                Currency currency = new Currency(ZERO_ID, name, code.toUpperCase(), sign);
                Currency resultCurrency = repository.save(currency);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                new ResponseHelper(resp, resultCurrency);
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            } catch (AlreadyExistsException e) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                ErrorResponse error = new ErrorResponse("Currency already exists");
                new ResponseHelper(resp, error);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse error = new ErrorResponse("Invalid parameters");
            new ResponseHelper(resp, error);
        }
    }

    private boolean isParametersValid(String name, String code, String sign) {
        if (name == null || name.isBlank()) {
            return false;
        }

        if (code == null || !code.matches("[A-z]{3}")) {
            return false;
        }

        if (sign == null || sign.isBlank() || sign.length() > 2 ) {
            return false;
        }

        return true;
    }
}
