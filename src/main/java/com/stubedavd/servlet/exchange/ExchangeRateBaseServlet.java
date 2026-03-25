package com.stubedavd.servlet.exchange;

import com.stubedavd.exception.NotFoundException;
import com.stubedavd.listener.ContextListener;
import com.stubedavd.mapper.ExchangeRateMapper;
import com.stubedavd.service.ExchangeRateService;
import com.stubedavd.servlet.BaseServlet;
import jakarta.servlet.ServletException;

public class ExchangeRateBaseServlet extends BaseServlet {

    protected ExchangeRateService exchangeRateService;
    protected ExchangeRateMapper exchangeRateMapper;

    @Override
    public void init() throws ServletException {

        super.init();

        exchangeRateService =
                (ExchangeRateService) getServletContext().getAttribute(ContextListener.EXCHANGE_RATE_SERVICE);

        if (exchangeRateService == null) {
            throw new NotFoundException("No exchange rate service found");
        }

        this.exchangeRateMapper =
                (ExchangeRateMapper) getServletContext().getAttribute(ContextListener.EXCHANGE_RATE_MAPPER);

        if (exchangeRateMapper == null) {
            throw new NotFoundException("Exchange rate mapper not found");
        }
    }
}
