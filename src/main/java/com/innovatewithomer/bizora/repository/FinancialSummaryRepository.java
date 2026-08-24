package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.report.FinancialSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class FinancialSummaryRepository
        implements FinancialSummaryRepositoryPort {

    @Override
    public FinancialSummary getFinancialSummary(
            LocalDate from,
            LocalDate to
    ) {

        String salesSql = """
                SELECT
                    COALESCE(SUM(total), 0)
                FROM sales
                WHERE DATE(created_at)
                    BETWEEN ? AND ?
                """;

        String purchasesSql = """
                SELECT
                    COALESCE(SUM(total), 0)
                FROM purchases
                WHERE DATE(created_at)
                    BETWEEN ? AND ?
                """;

        String expensesSql = """
                SELECT
                    COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE expense_date
                    BETWEEN ? AND ?
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection()
        ) {

            double totalSales =
                    getTotal(
                            connection,
                            salesSql,
                            from,
                            to
                    );

            double totalPurchases =
                    getTotal(
                            connection,
                            purchasesSql,
                            from,
                            to
                    );

            double totalExpenses =
                    getTotal(
                            connection,
                            expensesSql,
                            from,
                            to
                    );

            return new FinancialSummary(
                    from,
                    to,
                    totalSales,
                    totalPurchases,
                    totalExpenses
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate financial summary.",
                    e
            );
        }
    }

    private double getTotal(
            Connection connection,
            String sql,
            LocalDate from,
            LocalDate to
    ) throws Exception {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    from.toString()
            );

            statement.setString(
                    2,
                    to.toString()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    return resultSet.getDouble(1);
                }

                return 0;
            }
        }
    }
}