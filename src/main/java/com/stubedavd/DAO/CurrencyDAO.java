package com.stubedavd.DAO;

import com.stubedavd.DTO.CurrencyMap;
import com.stubedavd.DTO.DatabaseConnector;
import com.stubedavd.models.Currency;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {
    private static final String FIND_BY_CODE =
            "SELECT * FROM Currencies WHERE Code = ?;";
    private static final String FIND_ALL =
            "SELECT * FROM Currencies;";
    private static final String SAVE =
            "INSERT INTO Currencies(Code, FullName, Sign) VALUES (?,?,?) RETURNING ID;";

    private final Connection connection;

    public CurrencyDAO() throws IOException {
        this.connection = DatabaseConnector.getInstance().getConnection();
    }

    public List<Currency> findAll() throws IOException {
        List<Currency> currencies = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    CurrencyMap currencyMap = new CurrencyMap(resultSet);
                    currencies.add(currencyMap.getCurrency());
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return currencies;
    }

    public Currency findByCode(String code) throws IOException {
        if (code == null || code.isBlank()) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE)) {
            preparedStatement.setString(1, code);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    CurrencyMap currencyMap = new CurrencyMap(resultSet);
                    return currencyMap.getCurrency();
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return null;
    }

    public Currency save(Currency currency) throws IOException {
        Currency result = null;
        if (currency == null || isCurrencyExist(currency)) {
            return result;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE)) {
            String code = currency.getCode();
            String name = currency.getName();
            String sign = currency.getSign();
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, sign);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    result = new Currency(id, name, code, sign);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return result;
    }

    private boolean isCurrencyExist(Currency currency) throws IOException {
        if (currency == null) {
            return false;
        }
        return null != findByCode(currency.getCode());
    }
}
