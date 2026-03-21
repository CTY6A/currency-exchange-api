package com.stubedavd.repository;

import com.stubedavd.exception.AlreadyExistsException;
import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.CurrencyMap;
import com.stubedavd.utils.DataSource;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateRepository {
    private static final int INTEGRITY_CONSTRAINT_VIOLATION_CODE = 19;

    private final DataSource dataSource;

    public ExchangeRateRepository() throws InfrastructureException {
        this.dataSource = new DataSource();
    }

    public List<ExchangeRate> findAll() throws InfrastructureException {
        final String query =
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

        try (Connection connection = dataSource.getConnection()){
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<ExchangeRate> exchangeRates = new ArrayList<>();
                    while (resultSet.next()) {
                        exchangeRates.add(getExchangeRate(resultSet));
                    }
                    return exchangeRates;
                }
            }
        } catch (SQLException e) {
            throw new InfrastructureException(e);
        }
    }

    public Optional<ExchangeRate> findByPair(String baseCode, String targetCode) throws InfrastructureException {
        final String query =
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
                WHERE BaseCurrency.Code = ? AND TargetCurrency.Code = ?
            """;
        try (Connection connection = dataSource.getConnection();){
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
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
            throw new InfrastructureException(e);
        }
    }

    public ExchangeRate save(ExchangeRate exchangeRate) throws InfrastructureException {
        final String SAVE =
            """
                INSERT INTO ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate)
                VALUES (?,?,?)
                RETURNING ID
            """;

        try (Connection connection = dataSource.getConnection();){
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE)) {
                Currency baseCurrency = exchangeRate.getBaseCurrency();
                Currency targetCurrency = exchangeRate.getTargetCurrency();
                BigDecimal rate = exchangeRate.getRate();

                preparedStatement.setInt(1, baseCurrency.getId());
                preparedStatement.setInt(2, targetCurrency.getId());
                preparedStatement.setBigDecimal(3, rate);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    int id = resultSet.getInt("ID");
                    return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == INTEGRITY_CONSTRAINT_VIOLATION_CODE) {
                throw new AlreadyExistsException(e);
            }
            throw new InfrastructureException(e);
        }
    }

    public ExchangeRate update(ExchangeRate exchangeRate) throws InfrastructureException {

        String UPDATE =
            """
                UPDATE ExchangeRates
                SET Rate = ?
                WHERE
                    BaseCurrencyId = ? AND
                    TargetCurrencyId = ?
                RETURNING ID;
            """;
        try (Connection connection = dataSource.getConnection();){
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
                Currency baseCurrency = exchangeRate.getBaseCurrency();
                Currency targetCurrency = exchangeRate.getTargetCurrency();
                BigDecimal rate = exchangeRate.getRate();

                preparedStatement.setBigDecimal(1, rate);
                preparedStatement.setInt(2, baseCurrency.getId());
                preparedStatement.setInt(3, targetCurrency.getId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    int id = resultSet.getInt("ID");
                    return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                }
            }
        } catch (SQLException e) {
            throw new InfrastructureException(e);
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
