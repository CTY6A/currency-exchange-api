package com.stubedavd.servlet.currency;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.utils.Validator;
import com.stubedavd.model.Currency;
import com.stubedavd.exception.NotFoundException;

@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {

    private CurrencyRepository repository;

    @Override
    public void init() throws ServletException {
        super.init();

        repository =
                (CurrencyRepository) getServletContext().getAttribute("currencyRepository");

        if (repository == null) {
            throw new NotFoundException("Currency repository not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        Validator.validateOneCodePath(pathInfo);

        String code = pathInfo.substring(1);
        Optional<Currency> currencyOptional = repository.findByCode(code);

        if (currencyOptional.isPresent()) {
            sendJson(response, HttpServletResponse.SC_OK, currencyOptional.get());
        } else {
            throw new NotFoundException("Currency not found");
        }
    }
}
