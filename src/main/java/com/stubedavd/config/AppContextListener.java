package com.stubedavd.config;

import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.repository.JdbcCurrencyRepository;
import com.stubedavd.repository.JdbcExchangeRateRepository;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
        ExchangeRateRepository exchangeRateRepository = new JdbcExchangeRateRepository();

        servletContext.setAttribute("currencyRepository", currencyRepository);
        servletContext.setAttribute("exchangeRateRepository", exchangeRateRepository);
    }
}
