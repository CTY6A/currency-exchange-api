package com.stubedavd.repository;

import com.stubedavd.exception.DatabaseException;
import com.stubedavd.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRate> {

    Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws DatabaseException;

    ExchangeRate update(ExchangeRate exchangeRate) throws DatabaseException;
}
