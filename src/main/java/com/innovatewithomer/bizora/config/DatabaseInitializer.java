package com.innovatewithomer.bizora.config;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.infrastructure.migration.MigrationRunner;
import com.innovatewithomer.bizora.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            MigrationRunner migrationRunner =
                    new MigrationRunner();

            migrationRunner.run(connection);
            try (var statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode = WAL");
                statement.execute("PRAGMA synchronous = NORMAL");
            }

            AppLogger.info("Database initialized successfully.");

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to initialize database.",
                    e
            );
        }
    }
}
