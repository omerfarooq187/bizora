package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V5__CreateCustomersAndSuppliersTables
        implements Migration {

    @Override
    public int version() {
        return 5;
    }

    @Override
    public String description() {
        return "Create customers and suppliers tables";
    }

    @Override
    public void migrate(Connection connection)
            throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE customers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        phone TEXT,
                        email TEXT,
                        address TEXT,
                        created_at TEXT NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE suppliers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        phone TEXT,
                        email TEXT,
                        address TEXT,
                        created_at TEXT NOT NULL
                    )
                    """);
        }
    }
}