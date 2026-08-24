package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V2__CreateInventoryMovementsTable implements Migration {

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String description() {
        return "Create inventory movements table";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE inventory_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_id INTEGER NOT NULL,
                        movement_type TEXT NOT NULL,
                        quantity REAL NOT NULL,
                        reference_type TEXT,
                        reference_id INTEGER,
                        note TEXT,
                        created_at TEXT NOT NULL,

                        FOREIGN KEY (product_id)
                            REFERENCES products(id)
                    )
                    """);
        }
    }
}