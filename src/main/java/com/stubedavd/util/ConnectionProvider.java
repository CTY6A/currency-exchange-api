package com.stubedavd.util;

import com.stubedavd.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionProvider {

    private static final String DATABASE_PROPERTIES_PATH = "db.properties";

    private static final String DRIVER_PROPERTY_KEY = "driver";
    private static final String DATABASE_URL_PROPERTY_KEY = "databaseUrl";
    private static final String DATABASE_PATH_PROPERTY_KEY = "databasePath";

    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_IDLE = 2;
    public static final int CONNECTION_TIMEOUT_MS = 30000;
    public static final int IDLE_TIMEOUT_MS = 60000;
    public static final int MAX_LIFETIME_MS = 1800000;

    private static HikariDataSource dataSource;

    private static String driver;
    private static String databaseUrl;
    private static String databasePath;

    private ConnectionProvider() {

    }

    public static void init() {

        if (dataSource != null) {
            return;
        }

        readProperties();

        try {

            Class.forName(driver);
            URL resource = ConnectionProvider.class.getResource(databasePath);
            HikariConfig config = getHikariConfig(resource);

            dataSource = new HikariDataSource(config);
        } catch (ClassNotFoundException | FileNotFoundException | URISyntaxException e) {
            throw new DatabaseException("Database could not be initialized");
        }
    }

    private static void readProperties() {

        Properties properties = new Properties();

        try (InputStream inputStream =
                     ConnectionProvider.class.getResourceAsStream("/" + DATABASE_PROPERTIES_PATH)) {

            properties.load(inputStream);

            driver = properties.getProperty(DRIVER_PROPERTY_KEY);
            databaseUrl = properties.getProperty(DATABASE_URL_PROPERTY_KEY);
            databasePath = properties.getProperty(DATABASE_PATH_PROPERTY_KEY);
        } catch (IOException e) {
            throw new DatabaseException("Database properties could not be read");
        }
    }

    private static HikariConfig getHikariConfig(URL resource) throws FileNotFoundException, URISyntaxException {

        if (resource == null) {
            throw new FileNotFoundException(databasePath);
        }

        String jdbcUrl = databaseUrl + resource.toURI().getPath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName(driver);
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

    public static void close() {

        if (dataSource != null) {
            dataSource.close();
        }
    }
}