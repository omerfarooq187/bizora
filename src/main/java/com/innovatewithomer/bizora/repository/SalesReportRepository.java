package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.SalesReport;

import java.sql.*;
import java.time.LocalDate;

public class SalesReportRepository
        implements SalesReportRepositoryPort {

    @Override
    public SalesReport getSalesReport(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {

        String sql = """
                SELECT
                    COUNT(*) AS total_sales,
                    COALESCE(SUM(total), 0) AS total_revenue,
                    COALESCE(AVG(total), 0) AS average_sale_value
                FROM sales
                WHERE sale_status = 'COMPLETED'
                  AND created_at >= ?
                  AND created_at < ?
                """;

        /*
         * We use:
         *
         *     >= start
         *     < next day after 'to'
         *
         * rather than BETWEEN.
         *
         * This correctly includes the entire
         * final day when created_at contains time.
         */

        LocalDate nextDay =
                to.plusDays(1);

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    from.atStartOfDay().toString()
            );

            statement.setString(
                    2,
                    nextDay.atStartOfDay().toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {

                    return new SalesReport(
                            0,
                            0,
                            0
                    );
                }

                long totalSales =
                        resultSet.getLong(
                                "total_sales"
                        );

                double totalRevenue =
                        resultSet.getDouble(
                                "total_revenue"
                        );

                double averageSaleValue =
                        resultSet.getDouble(
                                "average_sale_value"
                        );

                return new SalesReport(
                        totalSales,
                        totalRevenue,
                        averageSaleValue
                );
            }
        }
    }

    /*
     * Convenience method for normal application use.
     *
     * The service can use this when it doesn't
     * need to participate in another transaction.
     */
    public SalesReport getSalesReport(
            LocalDate from,
            LocalDate to
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return getSalesReport(
                    connection,
                    from,
                    to
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to generate sales report.",
                    e
            );
        }
    }
}