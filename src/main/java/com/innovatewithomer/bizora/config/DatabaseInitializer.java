package com.innovatewithomer.bizora.config;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.infrastructure.migration.MigrationRunner;

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

            System.out.println(
                    "Database initialized successfully."
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to initialize database.",
                    e
            );
        }
    }
}