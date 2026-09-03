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
                WHERE sale_status = 'COMPLETED'
                  AND created_at >= ?
                  AND created_at < ?
                """;

        String costOfGoodsSoldSql = """
                SELECT
                    COALESCE(SUM(item.quantity * item.cost_price), 0)
                FROM sale_items item
                JOIN sales sale ON sale.id = item.sale_id
                WHERE sale.sale_status = 'COMPLETED'
                  AND sale.created_at >= ?
                  AND sale.created_at < ?
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
                            from.atStartOfDay().toString(),
                            to.plusDays(1).atStartOfDay().toString()
                    );

            double totalCostOfGoodsSold =
                    getTotal(
                            connection,
                            costOfGoodsSoldSql,
                            from.atStartOfDay().toString(),
                            to.plusDays(1).atStartOfDay().toString()
                    );

            double totalExpenses =
                    getTotal(
                            connection,
                            expensesSql,
                            from.toString(),
                            to.toString()
                    );

            return new FinancialSummary(
                    from,
                    to,
                    totalSales,
                    totalCostOfGoodsSold,
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
            String from,
            String to
    ) throws Exception {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    from
            );

            statement.setString(
                    2,
                    to
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
