package com.stubedavd.repository;

import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.model.Currency;

import java.util.Optional;

public interface CurrencyRepository extends CrudRepository<Currency> {
    Optional<Currency> findByCode(String code) throws InfrastructureException;
}
