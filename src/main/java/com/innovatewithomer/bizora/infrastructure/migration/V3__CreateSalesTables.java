package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V3__CreateSalesTables implements Migration {

    @Override
    public int version() {
        return 3;
    }

    @Override
    public String description() {
        return "Create sales and sale items tables";
    }

    @Override
    public void migrate(Connection connection)
            throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE sales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER,

                        invoice_number TEXT NOT NULL UNIQUE,

                        subtotal REAL NOT NULL DEFAULT 0,
                        discount REAL NOT NULL DEFAULT 0,
                        tax REAL NOT NULL DEFAULT 0,
                        total REAL NOT NULL DEFAULT 0,

                        payment_status TEXT NOT NULL,
                        sale_status TEXT NOT NULL,

                        created_at TEXT NOT NULL,
                        
                        FOREIGN KEY (customer_id)
                                       REFERENCES customers(id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE sale_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,

                        sale_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,

                        quantity REAL NOT NULL,
                        unit_price REAL NOT NULL,
                        cost_price REAL NOT NULL DEFAULT 0,
                        discount REAL NOT NULL DEFAULT 0,
                        subtotal REAL NOT NULL,

                        FOREIGN KEY (sale_id)
                            REFERENCES sales(id)
                            ON DELETE CASCADE,

                        FOREIGN KEY (product_id)
                            REFERENCES products(id)
                    )
                    """);
        }
    }
}
