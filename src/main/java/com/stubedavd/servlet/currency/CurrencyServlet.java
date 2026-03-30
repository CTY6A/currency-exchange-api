package com.stubedavd.servlet.currency;

import com.stubedavd.dto.response.CurrencyResponseDto;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.listener.ContextListener;
import com.stubedavd.mapper.CurrencyMapper;
import com.stubedavd.model.Currency;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {

    private CurrencyRepository currencyRepository;
    private CurrencyMapper currencyMapper;

    @Override
    public void init() throws ServletException {
        super.init();

        currencyRepository =
                (CurrencyRepository) getServletContext().getAttribute(ContextListener.CURRENCY_REPOSITORY);

        if (currencyRepository == null) {
            throw new NotFoundException("Currency repository not found");
        }

        this.currencyMapper =
                (CurrencyMapper) getServletContext().getAttribute(ContextListener.CURRENCY_MAPPER);

        if (currencyMapper == null) {
            throw new NotFoundException("Currency mapper not found");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();

        Validator.validateOneCodePath(pathInfo);

        String code = pathInfo.substring(1);

        Currency currency = currencyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Currency not found by code: " + code));

        CurrencyResponseDto currencyResponseDto = currencyMapper.toResponseDto(currency);

        sendJson(response, HttpServletResponse.SC_OK, currencyResponseDto);
    }
}
