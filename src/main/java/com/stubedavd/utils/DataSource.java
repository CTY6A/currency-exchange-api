package com.stubedavd.utils;

import com.stubedavd.exception.InfrastructureException;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static final String DRIVER = "org.sqlite.JDBC";
    private final static String DATABASE_URL = "jdbc:sqlite:";
    private final static String DATABASE_PATH = "/currencies.db";

    private final String url;

    private static DataSource instance;

    public DataSource() throws InfrastructureException {
        try {
            Class.forName(DRIVER);
            URL resource = DataSource.class.getResource(DATABASE_PATH);
            if (resource == null) {
                throw new FileNotFoundException(DATABASE_PATH);
            }
            this.url = DATABASE_URL + resource.getPath();
        } catch (ClassNotFoundException | FileNotFoundException e) {
            throw new InfrastructureException(e);
        }
    }

    public Connection getConnection() throws InfrastructureException {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new InfrastructureException();
        }
    }
}