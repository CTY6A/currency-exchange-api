package com.stubedavd.utils;

import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.repository.ExchangeRateRepository;

public class Main {
    public static void main(String[] args) throws InfrastructureException {
        ExchangeRateRepository repository = new ExchangeRateRepository();
        System.out.println(repository.findAll());
    }
}
