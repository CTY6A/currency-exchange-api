package com.stubedavd.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private static final String DRIVER = "org.sqlite.JDBC";
    private final static String DATABASE_URL = "jdbc:sqlite:";
    private final static String DATABASE_PATH = "/currencies.db";

    private final HikariDataSource dataSource;

    public DataSource() {
        try {
            Class.forName(DRIVER);
            URL resource = DataSource.class.getResource(DATABASE_PATH);
            HikariConfig config = getHikariConfig(resource);

            this.dataSource = new HikariDataSource(config);
        } catch (ClassNotFoundException | FileNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private HikariConfig getHikariConfig(URL resource) throws FileNotFoundException, URISyntaxException {
        if (resource == null) {
            throw new FileNotFoundException(DATABASE_PATH);
        }
        String jdbcUrl = DATABASE_URL + resource.toURI().getPath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName(DRIVER);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        return config;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }
}