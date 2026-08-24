package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.report.ExpenseReport;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseReportRepository
        implements ExpenseReportRepositoryPort {

    @Override
    public ExpenseReport getExpenseReport(
            LocalDate from,
            LocalDate to
    ) {

        String totalSql = """
                SELECT
                    COALESCE(SUM(amount), 0),
                    COUNT(*)
                FROM expenses
                WHERE expense_date BETWEEN ? AND ?
                """;

        String categorySql = """
                SELECT
                    category,
                    COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE expense_date BETWEEN ? AND ?
                GROUP BY category
                ORDER BY category
                """;

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            double totalExpenses;
            long expenseCount;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 totalSql
                         )) {

                statement.setString(
                        1,
                        from.toString()
                );

                statement.setString(
                        2,
                        to.toString()
                );

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (!resultSet.next()) {
                        totalExpenses = 0;
                        expenseCount = 0;
                    } else {
                        totalExpenses =
                                resultSet.getDouble(1);

                        expenseCount =
                                resultSet.getLong(2);
                    }
                }
            }

            Map<String, Double> expensesByCategory =
                    new LinkedHashMap<>();

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 categorySql
                         )) {

                statement.setString(
                        1,
                        from.toString()
                );

                statement.setString(
                        2,
                        to.toString()
                );

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    while (resultSet.next()) {

                        expensesByCategory.put(
                                resultSet.getString(
                                        "category"
                                ),
                                resultSet.getDouble(
                                        2
                                )
                        );
                    }
                }
            }

            return new ExpenseReport(
                    totalExpenses,
                    expenseCount,
                    expensesByCategory
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to generate expense report.",
                    e
            );
        }
    }
}