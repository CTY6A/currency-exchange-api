package com.stubedavd.utils;

import com.stubedavd.exception.InfrastructureException;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static final String DRIVER = "org.sqlite.JDBC";
    private final static String DATABASE_URL = "jdbc:sqlite:";
    private final static String DATABASE_PATH = "/currencies.db";

    private final String url;

    public DataSource() {
        try {
            Class.forName(DRIVER);
            URL resource = DataSource.class.getResource(DATABASE_PATH);
            if (resource == null) {
                throw new FileNotFoundException(DATABASE_PATH);
            }
            this.url = DATABASE_URL + resource.toURI().getPath();
        } catch (ClassNotFoundException | FileNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }
}