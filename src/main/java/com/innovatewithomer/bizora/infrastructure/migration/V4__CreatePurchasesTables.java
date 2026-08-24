package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V4__CreatePurchasesTables implements Migration {

    @Override
    public int version() {
        return 4;
    }

    @Override
    public String description() {
        return "Create purchases and purchase items tables";
    }

    @Override
    public void migrate(Connection connection)
            throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE purchases (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        invoice_number TEXT NOT NULL UNIQUE,
                        subtotal REAL NOT NULL DEFAULT 0,
                        discount REAL NOT NULL DEFAULT 0,
                        tax REAL NOT NULL DEFAULT 0,
                        total REAL NOT NULL DEFAULT 0,
                        payment_status TEXT NOT NULL,
                        purchase_status TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE purchase_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        purchase_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        unit_price REAL NOT NULL,
                        discount REAL NOT NULL DEFAULT 0,
                        subtotal REAL NOT NULL DEFAULT 0,

                        FOREIGN KEY (purchase_id)
                            REFERENCES purchases(id),

                        FOREIGN KEY (product_id)
                            REFERENCES products(id)
                    )
                    """);
        }
    }
}