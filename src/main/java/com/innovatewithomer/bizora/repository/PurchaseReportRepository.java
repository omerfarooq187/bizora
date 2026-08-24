package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.report.PurchaseReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseReportRepository
        implements PurchaseReportRepositoryPort {

    @Override
    public PurchaseReport getPurchaseReport(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null) {
            throw new IllegalArgumentException(
                    "From date cannot be null."
            );
        }

        if (to == null) {
            throw new IllegalArgumentException(
                    "To date cannot be null."
            );
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date."
            );
        }

        String sql = """
                SELECT
                    COUNT(*) AS total_purchases,
                    COALESCE(SUM(total), 0) AS total_cost,
                    COALESCE(AVG(total), 0) AS average_purchase_value
                FROM purchases
                WHERE purchase_status != 'CANCELLED'
                  AND created_at >= ?
                  AND created_at < ?
                """;

        LocalDateTime start =
                from.atStartOfDay();

        LocalDateTime end =
                to.plusDays(1).atStartOfDay();

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    start.toString()
            );

            statement.setString(
                    2,
                    end.toString()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return new PurchaseReport(
                            0,
                            0,
                            0
                    );
                }

                return new PurchaseReport(
                        resultSet.getLong(
                                "total_purchases"
                        ),
                        resultSet.getDouble(
                                "total_cost"
                        ),
                        resultSet.getDouble(
                                "average_purchase_value"
                        )
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to generate purchase report.",
                    e
            );
        }
    }
}