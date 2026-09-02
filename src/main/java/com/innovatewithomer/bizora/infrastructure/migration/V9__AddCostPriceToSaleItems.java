package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V9__AddCostPriceToSaleItems implements Migration {

    @Override
    public int version() {
        return 9;
    }

    @Override
    public String description() {
        return "Snapshot product cost on sale items";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        if (!hasCostPriceColumn(connection)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        ALTER TABLE sale_items
                        ADD COLUMN cost_price REAL NOT NULL DEFAULT 0
                        """);
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    UPDATE sale_items
                    SET cost_price = COALESCE(
                        (SELECT purchase_price
                         FROM products
                         WHERE products.id = sale_items.product_id),
                        0
                    )
                    WHERE cost_price = 0
                    """);
        }
    }

    private boolean hasCostPriceColumn(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(sale_items)")) {
            while (columns.next()) {
                if ("cost_price".equalsIgnoreCase(columns.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
