package com.stubedavd.DTO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private final static String DATABASE_URL = "jdbc:sqlite:src/main/resources/currencies.db";
    private final Connection connection;
    private static DatabaseConnector instance;

    private DatabaseConnector() {
        try {
            this.connection = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}