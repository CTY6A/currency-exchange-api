package com.stubedavd.DAO;

import com.stubedavd.DTO.CurrencyMap;
import com.stubedavd.DTO.DatabaseConnector;
import com.stubedavd.models.Currency;
import com.stubedavd.models.ExchangeRate;

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
            "INSERT INTO ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate) VALUES (?,?,?)";
    private static final String UPDATE =
            """
                UPDATE ExchangeRates
                    SET Rate = ?
                    WHERE
                        BaseCurrencyId = ? AND
                        TargetCurrencyId = ?;
            """;

    private final Connection connection;

    public ExchangeRateDAO() {
        this.connection = DatabaseConnector.getInstance().getConnection();
    }

    public List<ExchangeRate> findAll() {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    exchangeRates.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return exchangeRates;
    }

    public ExchangeRate findByPair(String baseCode, String targetCode){
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
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean save(ExchangeRate exchangeRate) {
        if (exchangeRate == null || isExchangeRateExist(exchangeRate)) {
            return false;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(ExchangeRate exchangeRate) {
        if (isExchangeRateExist(exchangeRate)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {
                preparedStatement.setBigDecimal(1, exchangeRate.getRate());
                preparedStatement.setInt(2, exchangeRate.getBaseCurrency().getId());
                preparedStatement.setInt(3, exchangeRate.getTargetCurrency().getId());
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private boolean isExchangeRateExist(ExchangeRate exchangeRate) {
        return exchangeRate != null && findByPair(exchangeRate.getBaseCurrency().getCode(), exchangeRate.getTargetCurrency().getCode()) != null;
    }

    private ExchangeRate mapResultSet(ResultSet resultSet) {
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

    private Currency findCurrencyByID(Integer id) {
        if (id == null || id < 1) {
            return null;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
            preparedStatement.setInt(1, id);
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
}
