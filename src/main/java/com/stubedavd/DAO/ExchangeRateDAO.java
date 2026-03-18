package com.stubedavd.DAO;

import com.stubedavd.DTO.CurrencyMap;
import com.stubedavd.DTO.DatabaseConnector;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ExchangeRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDAO {
    private static final String FIND_BY_ID =
            "SELECT * FROM Currencies WHERE ID = ?;";
    private static final String FIND_ALL =
            "SELECT * FROM ExchangeRates;";
    private static final String FIND_BY_PAIR =
            """
                SELECT * FROM ExchangeRates WHERE
                    BaseCurrencyId = (SELECT ID FROM Currencies WHERE Code = ?) AND
                    TargetCurrencyId = (SELECT ID FROM Currencies WHERE Code = ?);
            """;
    private static final String SAVE =
            "INSERT INTO ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate) VALUES (?,?,?) RETURNING ID;";
    private static final String UPDATE =
            """
                UPDATE ExchangeRates
                    SET Rate = ?
                    WHERE
                        BaseCurrencyId = ? AND
                        TargetCurrencyId = ?
                    RETURNING ID;
            """;

    private final Connection connection;

    public ExchangeRateDAO() throws IOException {
        this.connection = DatabaseConnector.getInstance().getConnection();
    }

    public List<ExchangeRate> findAll() throws IOException {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    exchangeRates.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return exchangeRates;
    }

    public ExchangeRate findByPair(String baseCode, String targetCode) throws IOException {
        if (baseCode == null || targetCode == null || baseCode.isBlank() || targetCode.isBlank()) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_PAIR)) {
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return null;
    }

    public ExchangeRate save(ExchangeRate exchangeRate) throws IOException {
        ExchangeRate result = null;
        if (exchangeRate == null || isExchangeRateExist(exchangeRate)) {
            return result;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE)) {
            Currency baseCurrency = exchangeRate.getBaseCurrency();
            Currency targetCurrency = exchangeRate.getTargetCurrency();
            BigDecimal rate = exchangeRate.getRate();
            preparedStatement.setInt(1, baseCurrency.getId());
            preparedStatement.setInt(2, targetCurrency.getId());
            preparedStatement.setBigDecimal(3, rate);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    result = new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return result;
    }

    public ExchangeRate update(ExchangeRate exchangeRate) throws IOException {
        ExchangeRate result = null;
        if (isExchangeRateExist(exchangeRate)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
                Currency baseCurrency = exchangeRate.getBaseCurrency();
                Currency targetCurrency = exchangeRate.getTargetCurrency();
                BigDecimal rate = exchangeRate.getRate();
                preparedStatement.setBigDecimal(1, rate);
                preparedStatement.setInt(2, baseCurrency.getId());
                preparedStatement.setInt(3, targetCurrency.getId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("ID");
                        result = new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                    }
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
        return result;
    }

    private boolean isExchangeRateExist(ExchangeRate exchangeRate) throws IOException {
        return exchangeRate != null && findByPair(exchangeRate.getBaseCurrency().getCode(), exchangeRate.getTargetCurrency().getCode()) != null;
    }

    private ExchangeRate mapResultSet(ResultSet resultSet) throws IOException {
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
            throw new IOException(e);
        }

        return new ExchangeRate(id, findCurrencyByID(BaseCurrencyId), findCurrencyByID(TargetCurrencyId), Rate);
    }

    private Currency findCurrencyByID(Integer id) throws IOException {
        if (id == null || id < 1) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
            preparedStatement.setInt(1, id);
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
}
