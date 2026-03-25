package com.stubedavd.servlet.currency;

import com.stubedavd.dto.request.CurrencyRequestDto;
import com.stubedavd.dto.response.CurrencyResponseDto;
import com.stubedavd.exception.NotFoundException;
import com.stubedavd.listener.ContextListener;
import com.stubedavd.mapper.CurrencyMapper;
import com.stubedavd.model.Currency;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.servlet.BaseServlet;
import com.stubedavd.utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {

    private CurrencyRepository currencyRepository;
    private CurrencyMapper currencyMapper;

    @Override
    public void init() throws ServletException {

        super.init();

        this.currencyRepository =
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

        List<Currency> currencies = currencyRepository.findAll();

        List<CurrencyResponseDto> currenciesResponseDto =
                currencies.stream().map(currencyMapper::toResponseDto).toList();

        sendJson(response, HttpServletResponse.SC_OK, currenciesResponseDto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        Validator.validateCurrency(name, code, sign);

        CurrencyRequestDto currencyRequestDto = currencyMapper.toRequestDto(name, code.toUpperCase(), sign);

        Currency currency = currencyMapper.toModel(currencyRequestDto);
        Currency resultCurrency = currencyRepository.save(currency);

        CurrencyResponseDto currencyResponseDto = currencyMapper.toResponseDto(resultCurrency);

        sendJson(response, HttpServletResponse.SC_CREATED, currencyResponseDto);
    }
}
