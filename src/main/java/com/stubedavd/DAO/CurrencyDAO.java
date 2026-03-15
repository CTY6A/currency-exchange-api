package com.stubedavd.DAO;

import com.stubedavd.DTO.CurrencyMap;
import com.stubedavd.DTO.DatabaseConnector;
import com.stubedavd.models.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {
    private static final String FIND_BY_CODE =
            "SELECT * FROM Currencies WHERE Code = ?;";
    private static final String FIND_ALL =
            "SELECT * FROM Currencies;";
    private static final String SAVE =
            "INSERT INTO Currencies(Code, FullName, Sign) VALUES (?,?,?);";

    private final Connection connection;

    public CurrencyDAO() {
        this.connection = DatabaseConnector.getInstance().getConnection();
    }

    public List<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    currencies.add(CurrencyMap.mapResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return currencies;
    }

    public Currency findByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE)) {
            preparedStatement.setString(1, code);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return CurrencyMap.mapResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean save(Currency currency) {
        if (currency == null || isCurrencyExist(currency)) {
            return false;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCurrencyExist(Currency currency) {
        if (currency == null) {
            return false;
        }
        return null != findByCode(currency.getCode());
    }
}
