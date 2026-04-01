package com.stubedavd.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.stubedavd.mapper.CurrencyMapper;
import com.stubedavd.mapper.ExchangeMapper;
import com.stubedavd.mapper.ExchangeRateMapper;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.impl.JdbcCurrencyRepository;
import com.stubedavd.repository.impl.JdbcExchangeRateRepository;
import com.stubedavd.service.ExchangeRateService;
import com.stubedavd.service.ExchangeService;
import com.stubedavd.service.impl.ExchangeRateServiceImpl;
import com.stubedavd.service.impl.ExchangeServiceImpl;
import com.stubedavd.util.ConnectionProvider;

@WebListener
public class ContextListener implements ServletContextListener {

    public static final String CURRENCY_REPOSITORY = "currencyRepository";
    public static final String EXCHANGE_RATE_REPOSITORY = "exchangeRateRepository";
    public static final String CURRENCY_MAPPER = "currencyMapper";
    public static final String EXCHANGE_RATE_MAPPER = "exchangeRateMapper";
    public static final String EXCHANGE_MAPPER = "exchangeMapper";
    public static final String EXCHANGE_RATE_SERVICE = "exchangeRateService";
    public static final String EXCHANGE_SERVICE = "exchangeService";
    public static final String OBJECT_MAPPER = "objectMapper";

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ConnectionProvider.init();

        ObjectMapper objectMapper = new ObjectMapper();

        ServletContext servletContext = sce.getServletContext();

        CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
        ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();

        CurrencyMapper currencyMapper = CurrencyMapper.INSTANCE;
        ExchangeRateMapper exchangeRateMapper = ExchangeRateMapper.INSTANCE;
        ExchangeMapper exchangeMapper = ExchangeMapper.INSTANCE;

        ExchangeRateService exchangeRateService =
                new ExchangeRateServiceImpl(exchangeRateRepository, currencyRepository, exchangeRateMapper);
        ExchangeService exchangeService =
                new ExchangeServiceImpl(exchangeRateRepository, exchangeMapper);

        servletContext.setAttribute(OBJECT_MAPPER, objectMapper);

        servletContext.setAttribute(CURRENCY_REPOSITORY, currencyRepository);
        servletContext.setAttribute(EXCHANGE_RATE_REPOSITORY, exchangeRateRepository);

        servletContext.setAttribute(CURRENCY_MAPPER, currencyMapper);
        servletContext.setAttribute(EXCHANGE_RATE_MAPPER, exchangeRateMapper);
        servletContext.setAttribute(EXCHANGE_MAPPER, exchangeMapper);

        servletContext.setAttribute(EXCHANGE_RATE_SERVICE, exchangeRateService);
        servletContext.setAttribute(EXCHANGE_SERVICE, exchangeService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionProvider.close();
    }
}
