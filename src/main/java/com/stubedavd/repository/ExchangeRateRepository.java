package com.stubedavd.repository;

import com.stubedavd.exception.InfrastructureException;
import com.stubedavd.utils.CurrencyMap;
import com.stubedavd.utils.DatabaseConnector;
import com.stubedavd.model.Currency;
import com.stubedavd.model.ExchangeRate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateRepository {
    private final DatabaseConnector databaseConnector;

    public ExchangeRateRepository() throws InfrastructureException {
        this.databaseConnector = new DatabaseConnector();
    }

    public List<ExchangeRate> findAll() throws InfrastructureException {
        String query = "SELECT * FROM ExchangeRates";

        try (Connection connection = databaseConnector.getConnection();){
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<ExchangeRate> exchangeRates = new ArrayList<>();
                    while (resultSet.next()) {
                        exchangeRates.add(mapResultSet(resultSet));
                    }
                    return exchangeRates;
                }
            }
        } catch (SQLException e) {
            throw new InfrastructureException(e);
        }
    }

    public ExchangeRate findByPair(String baseCode, String targetCode) throws InfrastructureException {
        String query =
            """
                SELECT *
                FROM ExchangeRates
                WHERE
                    BaseCurrencyId = (SELECT ID FROM Currencies WHERE Code = ?) AND
                    TargetCurrencyId = (SELECT ID FROM Currencies WHERE Code = ?);
            """;
        try (Connection connection = databaseConnector.getConnection();){
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, baseCode);
                preparedStatement.setString(2, targetCode);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapResultSet(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            throw new InfrastructureException(e);
        }

        return null;
    }

    public ExchangeRate save(ExchangeRate exchangeRate) throws InfrastructureException {

        String SAVE = "INSERT INTO ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate) VALUES (?,?,?) RETURNING ID;";
        try (Connection connection = databaseConnector.getConnection();){
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
            throw new InfrastructureException(e);
        }
    }

    public ExchangeRate update(ExchangeRate exchangeRate) throws InfrastructureException {

        String UPDATE = """
                    UPDATE ExchangeRates
                        SET Rate = ?
                        WHERE
                            BaseCurrencyId = ? AND
                            TargetCurrencyId = ?
                        RETURNING ID;
                """;
        try (Connection connection = databaseConnector.getConnection();){
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

    private boolean isExchangeRateExist(ExchangeRate exchangeRate) throws InfrastructureException {
        return exchangeRate != null && findByPair(exchangeRate.getBaseCurrency().getCode(), exchangeRate.getTargetCurrency().getCode()) != null;
    }

    private ExchangeRate mapResultSet(ResultSet resultSet) throws InfrastructureException {
        int id;
        int BaseCurrencyId;
        int TargetCurrencyId;
        BigDecimal Rate;

        try {
            id = resultSet.getInt("ID");
            BaseCurrencyId = resultSet.getInt("BaseCurrencyId");
            TargetCurrencyId = resultSet.getInt("TargetCurrencyId");
            Rate = resultSet.getBigDecimal("Rate");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new ExchangeRate(id, findCurrencyByID(BaseCurrencyId), findCurrencyByID(TargetCurrencyId), Rate);
    }

    private Currency findCurrencyByID(Integer id) throws InfrastructureException {
        if (id == null || id < 1) {
            return null;
        }

        String FIND_BY_ID = "SELECT * FROM Currencies WHERE ID = ?;";
        try (Connection connection = databaseConnector.getConnection();){
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        CurrencyMap currencyMap = new CurrencyMap(resultSet);
                        return currencyMap.getCurrency();
                    }
                }
            }
        } catch (SQLException e) {
            throw new InfrastructureException(e);
        }

        return null;
    }
}
