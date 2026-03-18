package com.stubedavd.servlets;

import com.stubedavd.DAO.CurrencyDAO;
import com.stubedavd.DTO.ResponseHelper;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ErrorResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private static final int ZERO_ID = 1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            CurrencyDAO dao = new CurrencyDAO();
            List<Currency> currencies = dao.findAll();
            new ResponseHelper(resp, currencies);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorResponse error = new ErrorResponse("Database is unavailable");
            new ResponseHelper(resp, error);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        if (isParametersValid(name, code, sign)) {
            try {
                CurrencyDAO dao = new CurrencyDAO();
                Currency currency = dao.findByCode(code);
                if (currency == null) {
                    currency = new Currency(ZERO_ID, name, code.toUpperCase(), sign);
                    Currency resultCurrency = dao.save(currency);
                    if (resultCurrency == null) {
                        throw new IOException("Currency could not be saved");
                    }
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    new ResponseHelper(resp, resultCurrency);
                } else {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    ErrorResponse error = new ErrorResponse("Currency already exists");
                    new ResponseHelper(resp, error);
                }
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
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
