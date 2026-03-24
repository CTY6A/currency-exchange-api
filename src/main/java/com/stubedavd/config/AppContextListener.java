package com.stubedavd.config;

import com.stubedavd.service.ExchangeRateService;
import com.stubedavd.service.ExchangeRateServiceImpl;
import com.stubedavd.service.ExchangeService;
import com.stubedavd.service.ExchangeServiceImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.repository.JdbcExchangeRateRepository;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
        ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();

        ExchangeRateService exchangeRateService = new ExchangeRateServiceImpl(exchangeRateRepository, currencyRepository);
        ExchangeService exchangeService = new ExchangeServiceImpl(exchangeRateRepository, currencyRepository);

        servletContext.setAttribute("currencyRepository", currencyRepository);
        servletContext.setAttribute("exchangeRateRepository", exchangeRateRepository);

        servletContext.setAttribute("exchangeRateService", exchangeRateService);
        servletContext.setAttribute("exchangeService", exchangeService);
    }
}
