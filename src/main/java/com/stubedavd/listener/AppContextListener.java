package com.stubedavd.listener;

import com.stubedavd.service.ExchangeRateService;
import com.stubedavd.service.impl.ExchangeRateServiceImpl;
import com.stubedavd.service.ExchangeService;
import com.stubedavd.service.impl.ExchangeServiceImpl;
import com.stubedavd.utils.ConnectionProvider;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.impl.JdbcCurrencyRepository;
import com.stubedavd.repository.impl.JdbcExchangeRateRepository;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConnectionProvider.init();

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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionProvider.close();
    }
}
