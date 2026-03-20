package com.stubedavd.utils;

import com.stubedavd.model.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyMap {
    private final Currency currency;

    public CurrencyMap(ResultSet resultSet) {
        int id;
        String name;
        String code;
        String sign;

        try {
            id = resultSet.getInt("ID");
            name = resultSet.getString("FullName");
            code = resultSet.getString("Code");
            sign = resultSet.getString("Sign");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.currency = new Currency(id, name, code, sign);
    }

    public Currency getCurrency() {
        return currency;
    }
}
