package com.stubedavd.repository.impl;

import com.stubedavd.exception.AlreadyExistException;
import com.stubedavd.exception.DatabaseException;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;
import com.stubedavd.repository.ExchangeRateRepository;
import com.stubedavd.util.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExchangeRateRepository implements ExchangeRateRepository {

    public static final String FIND_ALL_QUERY =
                """
                    SELECT ExchangeRates.ID AS "ExchangeRates.ID",
                            BaseCurrency.ID AS "BaseCurrency.ID",
                                BaseCurrency.Code AS "BaseCurrency.Code",
                                BaseCurrency.FullName AS "BaseCurrency.FullName",
                                BaseCurrency.Sign AS "BaseCurrency.Sign",
                            TargetCurrency.ID AS "TargetCurrency.ID",
                                TargetCurrency.Code AS "TargetCurrency.Code",
                                TargetCurrency.FullName AS "TargetCurrency.FullName",
                                TargetCurrency.Sign AS "TargetCurrency.Sign",
                            Rate
                    FROM ExchangeRates
                    JOIN Currencies
                        BaseCurrency ON BaseCurrency.ID = ExchangeRates.BaseCurrencyId
                    JOIN Currencies
                        TargetCurrency on TargetCurrency.ID = ExchangeRates.TargetCurrencyId
              """;

    public static final String FIND_BY_CODES_QUERY =
            FIND_ALL_QUERY +
                """
                    WHERE BaseCurrency.Code = ? AND TargetCurrency.Code = ?
                """;

    public static final String SAVE_QUERY =
                """
                    INSERT INTO ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate)
                    VALUES (?,?,?)
                    RETURNING ID
                """;

    public static final String UPDATE_QUERY =
                """
                    UPDATE ExchangeRates
                    SET Rate = ?
                    WHERE
                        BaseCurrencyId = ? AND
                        TargetCurrencyId = ?
                    RETURNING ID;
                """;

    public List<ExchangeRate> findAll() throws DatabaseException {

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_QUERY)){

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    List<ExchangeRate> exchangeRates = new ArrayList<>();

                    while (resultSet.next()) {
                        exchangeRates.add(getExchangeRate(resultSet));
                    }

                    return exchangeRates;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database is not available");
        }
    }

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws DatabaseException {

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODES_QUERY)) {

                preparedStatement.setString(1, baseCode);
                preparedStatement.setString(2, targetCode);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        return Optional.of(getExchangeRate(resultSet));
                    }

                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database is not available");
        }
    }

    private static final int INTEGRITY_CONSTRAINT_VIOLATION_CODE = 19;

    public ExchangeRate save(ExchangeRate exchangeRate) throws DatabaseException, AlreadyExistException {

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_QUERY)) {

                Currency baseCurrency = exchangeRate.baseCurrency();
                Currency targetCurrency = exchangeRate.targetCurrency();
                BigDecimal rate = exchangeRate.rate();

                preparedStatement.setInt(1, baseCurrency.id());
                preparedStatement.setInt(2, targetCurrency.id());
                preparedStatement.setBigDecimal(3, rate);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    resultSet.next();

                    int id = resultSet.getInt("ID");

                    return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                }
            }
        } catch (SQLException e) {

            if (e.getErrorCode() == INTEGRITY_CONSTRAINT_VIOLATION_CODE) {
                throw new AlreadyExistException("Exchange rate already exists");
            }

            throw new DatabaseException("Database is not available");
        }
    }

    public ExchangeRate update(ExchangeRate exchangeRate) throws DatabaseException {

        try (Connection connection = ConnectionProvider.getConnection()){

            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_QUERY)) {

                Currency baseCurrency = exchangeRate.baseCurrency();
                Currency targetCurrency = exchangeRate.targetCurrency();
                BigDecimal rate = exchangeRate.rate();

                preparedStatement.setBigDecimal(1, rate);
                preparedStatement.setInt(2, baseCurrency.id());
                preparedStatement.setInt(3, targetCurrency.id());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    resultSet.next();

                    int id = resultSet.getInt("ID");

                    return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database is not available");
        }
    }

    private ExchangeRate getExchangeRate(ResultSet resultSet) throws SQLException {

        return new ExchangeRate(
                resultSet.getInt("ExchangeRates.ID"),
                new Currency(
                        resultSet.getInt("BaseCurrency.ID"),
                        resultSet.getString("BaseCurrency.FullName"),
                        resultSet.getString("BaseCurrency.Code"),
                        resultSet.getString("BaseCurrency.Sign")
                ),
                new Currency(
                        resultSet.getInt("TargetCurrency.ID"),
                        resultSet.getString("TargetCurrency.FullName"),
                        resultSet.getString("TargetCurrency.Code"),
                        resultSet.getString("TargetCurrency.Sign")
                ),
                resultSet.getBigDecimal("Rate")
        );
    }
}
