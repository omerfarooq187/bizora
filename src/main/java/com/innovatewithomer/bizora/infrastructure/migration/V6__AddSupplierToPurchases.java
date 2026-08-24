package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V6__AddSupplierToPurchases
        implements Migration {

    @Override
    public int version() {
        return 6;
    }

    @Override
    public String description() {
        return "Add supplier to purchases";
    }

    @Override
    public void migrate(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                ALTER TABLE purchases
                ADD COLUMN supplier_id INTEGER
                """);
        }
    }
}