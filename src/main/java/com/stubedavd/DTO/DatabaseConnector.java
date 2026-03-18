package com.stubedavd.DTO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DRIVER = "org.sqlite.JDBC";
    private final static String DATABASE_URL = "jdbc:sqlite:";
    private final static String DATABASE_PATH = "/currencies.db";

    private final Connection connection;

    private static DatabaseConnector instance;

    private DatabaseConnector() throws IOException {
        try {
            Class.forName(DRIVER);
            URL resource = DatabaseConnector.class.getResource(DATABASE_PATH);
            if (resource == null) {
                throw new FileNotFoundException(DATABASE_PATH);
            }
            String path = resource.getPath();
            String url = DATABASE_URL + path;
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException | FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    public static synchronized DatabaseConnector getInstance() throws IOException {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}