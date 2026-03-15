package com.stubedavd.DTO;

import com.stubedavd.models.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyMap {
    public static Currency mapResultSet(ResultSet resultSet) {
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

        return new Currency(id, name, code, sign);
    }
}
