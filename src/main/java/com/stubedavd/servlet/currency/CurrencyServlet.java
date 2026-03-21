package com.stubedavd.servlet.currency;

import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.ResponseHelper;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse error = new ErrorResponse("A required form field is missing");
            new ResponseHelper(resp, error);
        } else {
            String code = pathInfo.substring(1);
            try {
                JdbcCurrencyRepository dao = new JdbcCurrencyRepository();
                Optional<Currency> currencyOptional = dao.findByCode(code);
                if (currencyOptional.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    ErrorResponse error = new ErrorResponse("Currency not found");
                    new ResponseHelper(resp, error);
                } else {
                    new ResponseHelper(resp, currencyOptional.get());
                }
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse error = new ErrorResponse("Database is unavailable");
                new ResponseHelper(resp, error);
            }
        }
    }
}
