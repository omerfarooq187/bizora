package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V1__CreateProductsTable implements Migration {

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String description() {
        return "Create products table";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        sku TEXT UNIQUE,
                        selling_price REAL NOT NULL DEFAULT 0,
                        purchase_price REAL NOT NULL DEFAULT 0,
                        stock_quantity REAL NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL
                    )
                    """);
        }
    }
}