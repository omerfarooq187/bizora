package com.innovatewithomer.bizora.infrastructure;

import com.innovatewithomer.bizora.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static String jdbcUrl;

    private DatabaseManager() {
    }

    private static synchronized String resolveJdbcUrl() {
        if (jdbcUrl == null) {
            jdbcUrl = DatabaseConfig.getJdbcUrl();
        }
        return jdbcUrl;
    }

    public static Connection getConnection()
            throws SQLException {

        Connection connection =
                DriverManager.getConnection(resolveJdbcUrl());

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
            statement.execute(
                    "PRAGMA busy_timeout = 5000"
            );
        }

        return connection;
    }

    public static void setJdbcUrl(String jdbcUrl) {
        DatabaseManager.jdbcUrl = jdbcUrl;
    }

    public static void resetJdbcUrl() {
        DatabaseManager.jdbcUrl = null;
    }
}
