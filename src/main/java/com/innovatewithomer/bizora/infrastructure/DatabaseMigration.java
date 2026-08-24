package com.innovatewithomer.bizora.infrastructure;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseMigration {

    private DatabaseMigration() {
    }

    public static void createMigrationTable(Connection connection)
            throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version INTEGER PRIMARY KEY,
                        description TEXT NOT NULL,
                        applied_at TEXT NOT NULL
                    )
                    """);
        }
    }
}