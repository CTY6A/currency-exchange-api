package com.stubedavd.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.model.Currency;
import com.stubedavd.model.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();

    private CurrencyRepository repository;

    @Override
    public void init() throws ServletException {
        super.init();

        repository =
                (CurrencyRepository) getServletContext().getAttribute("currencyRepository");

        if (repository == null) {
            throw new IllegalStateException("currencyRepository not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(
                    "A required form field is missing"
            ));
        } else {
            String code = pathInfo.substring(1);
            try {
                Optional<Currency> currencyOptional = repository.findByCode(code);
                if (currencyOptional.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(resp.getWriter(), new ErrorResponse(
                            "Currency not found"
                    ));
                } else {
                    mapper.writeValue(resp.getWriter(), currencyOptional.get());
                }
            } catch (InfrastructureException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(
                        "Database is unavailable"
                ));            }
        }
    }
}
