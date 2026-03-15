package com.stubedavd.DTO;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseConnector {
    private static final String DRIVER = "org.sqlite.JDBC";
    private final static String DATABASE_URL = "jdbc:sqlite:";
    private final static String DATABASE_PATH = "/currencies.db";

    private final Connection connection;

    private static DatabaseConnector instance;

    private DatabaseConnector() {
        try {
            Class.forName(DRIVER);
            URL resource = DatabaseConnector.class.getResource(DATABASE_PATH);
            String path = Objects.requireNonNull(resource).getPath();
            String url = DATABASE_URL + path;
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}