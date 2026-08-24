package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V7__CreatePayments
        implements Migration {

    @Override
    public int version() {
        return 7;
    }

    @Override
    public String description() {
        return "Create payments";
    }

    @Override
    public void migrate(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                CREATE TABLE payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    sale_id INTEGER NOT NULL,

                    amount REAL NOT NULL,

                    payment_method TEXT NOT NULL,

                    reference TEXT,

                    created_at TEXT NOT NULL,

                    FOREIGN KEY (sale_id)
                        REFERENCES sales(id)
                )
                """);
        }
    }
}