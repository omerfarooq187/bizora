package com.innovatewithomer.bizora.infrastructure;

import com.innovatewithomer.bizora.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static String jdbcUrl =
            DatabaseConfig.getJdbcUrl();

    private DatabaseManager() {
    }

    public static Connection getConnection()
            throws SQLException {

        Connection connection =
                DriverManager.getConnection(jdbcUrl);

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }

        return connection;
    }

    public static void setJdbcUrl(String jdbcUrl) {
        DatabaseManager.jdbcUrl = jdbcUrl;
    }

    public static void resetJdbcUrl() {
        DatabaseManager.jdbcUrl =
                DatabaseConfig.getJdbcUrl();
    }
}