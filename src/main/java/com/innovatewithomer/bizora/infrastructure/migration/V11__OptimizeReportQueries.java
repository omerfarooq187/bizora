package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V11__OptimizeReportQueries implements Migration {

    @Override
    public int version() {
        return 11;
    }

    @Override
    public String description() {
        return "Optimize report date range queries";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_sales_status_date "
                            + "ON sales(sale_status, created_at)"
            );
        }
    }
}
