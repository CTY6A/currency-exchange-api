package com.stubedavd.utils;

import com.stubedavd.exception.InfrastructureException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionProvider {

    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DATABASE_URL = "jdbc:sqlite:";
    private static final String DATABASE_PATH = "/currencies.db";

    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_IDLE = 2;
    public static final int CONNECTION_TIMEOUT_MS = 30000;
    public static final int IDLE_TIMEOUT_MS = 60000;
    public static final int MAX_LIFETIME_MS = 1800000;

    private static HikariDataSource dataSource;

    private ConnectionProvider() {

    }

    public static void init() {

        if (dataSource != null) {
            return;
        }

        try {

            Class.forName(DRIVER);
            URL resource = ConnectionProvider.class.getResource(DATABASE_PATH);
            HikariConfig config = getHikariConfig(resource);

            dataSource = new HikariDataSource(config);
        } catch (ClassNotFoundException | FileNotFoundException | URISyntaxException e) {
            throw new InfrastructureException("Database could not be initialized");
        }
    }

    private static HikariConfig getHikariConfig(URL resource) throws FileNotFoundException, URISyntaxException {

        if (resource == null) {
            throw new FileNotFoundException(DATABASE_PATH);
        }

        String jdbcUrl = DATABASE_URL + resource.toURI().getPath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName(DRIVER);
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);

        return config;
    }

    public static Connection getConnection() throws SQLException {

        if (dataSource == null) {
            init();
        }

        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {

        if (dataSource == null) {
            init();
        }

        return dataSource;
    }

    public static void close() {

        if (dataSource != null) {
            dataSource.close();
        }
    }
}