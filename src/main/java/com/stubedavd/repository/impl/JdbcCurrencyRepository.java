package com.stubedavd.repository.impl;

import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.DatabaseException;
import com.stubedavd.repository.CurrencyRepository;
import com.stubedavd.util.ConnectionProvider;
import com.stubedavd.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyRepository implements CurrencyRepository {

    public List<Currency> findAll() throws DatabaseException {

        final String query = "SELECT * FROM Currencies";

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)){

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    List<Currency> currencies = new ArrayList<>();

                    while (resultSet.next()) {
                        currencies.add(getCurrency(resultSet));
                    }

                    return currencies;
                }
            }
        }  catch (SQLException e) {
            throw new DatabaseException("Database is not available");
        }
    }

    public Optional<Currency> findByCode(String code) throws DatabaseException {

        final String query = "SELECT * FROM Currencies WHERE Code = ?";

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, code);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        return Optional.of(getCurrency(resultSet));
                    }

                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database is not available");
        }
    }

    private static final int INTEGRITY_CONSTRAINT_VIOLATION_CODE = 19;

    public Currency save(Currency currency) throws DatabaseException, AlreadyExistException {

        final String query = "INSERT INTO Currencies(Code, FullName, Sign) VALUES (?,?,?) RETURNING ID";

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                String code = currency.getCode();
                String name = currency.getName();
                String sign = currency.getSign();

                preparedStatement.setString(1, code);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, sign);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    resultSet.next();
                    int id = resultSet.getInt("ID");

                    return new Currency(id, name, code, sign);
                }
            }
        } catch (SQLException e) {

            if (e.getErrorCode() == INTEGRITY_CONSTRAINT_VIOLATION_CODE) {
                throw new AlreadyExistException("Currency already exists");
            }

            throw new DatabaseException("Database is not available");
        }
    }

    private Currency getCurrency(ResultSet resultSet) throws SQLException {

        return new Currency(
                resultSet.getInt("ID"),
                resultSet.getString("FullName"),
                resultSet.getString("Code"),
                resultSet.getString("Sign")
        );
    }
}
