package com.stubedavd.DTO;

import com.stubedavd.models.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyMap {
    public static Currency mapResultSet(ResultSet resultSet) {
        int id;
        String code;
        String fullName;
        String sign;

        try {
            id = resultSet.getInt("ID");
            code = resultSet.getString("Code");
            fullName = resultSet.getString("FullName");
            sign = resultSet.getString("Sign");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new Currency(id, code, fullName, sign);
    }
}
