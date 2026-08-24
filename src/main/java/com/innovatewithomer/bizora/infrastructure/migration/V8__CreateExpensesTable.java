package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V8__CreateExpensesTable
        implements Migration {

    @Override
    public int version() {
        return 8;
    }

    @Override
    public String description() {
        return "Create expenses table";
    }

    @Override
    public void migrate(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                CREATE TABLE expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);
        }
    }
}