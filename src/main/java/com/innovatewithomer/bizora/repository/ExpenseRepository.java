package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository
        implements ExpenseRepositoryPort {

    @Override
    public Expense save(
            Connection connection,
            Expense expense
    ) throws SQLException {

        String sql = """
                INSERT INTO expenses
                (
                    category,
                    description,
                    amount,
                    expense_date,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(
                    1,
                    expense.getCategory()
            );

            statement.setString(
                    2,
                    expense.getDescription()
            );

            statement.setDouble(
                    3,
                    expense.getAmount()
            );

            statement.setString(
                    4,
                    expense.getExpenseDate()
                            .toString()
            );

            statement.setString(
                    5,
                    expense.getCreatedAt()
                            .toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    expense.setId(
                            keys.getLong(1)
                    );
                }
            }
        }

        return expense;
    }

    @Override
    public Expense findById(
            Long id
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findById(
                    connection,
                    id
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find expense.",
                    e
            );
        }
    }

    @Override
    public Expense findById(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    category,
                    description,
                    amount,
                    expense_date,
                    created_at
                FROM expenses
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    id
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }
        }
    }

    @Override
    public List<Expense> findAll() {

        String sql = """
                SELECT
                    id,
                    category,
                    description,
                    amount,
                    expense_date,
                    created_at
                FROM expenses
                ORDER BY id DESC
                """;

        List<Expense> expenses =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                expenses.add(
                        mapRow(resultSet)
                );
            }

            return expenses;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load expenses.",
                    e
            );
        }
    }

    @Override
    public void delete(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
                DELETE FROM expenses
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    id
            );

            statement.executeUpdate();
        }
    }

    private Expense mapRow(
            ResultSet resultSet
    ) throws SQLException {

        Expense expense =
                new Expense();

        expense.setId(
                resultSet.getLong("id")
        );

        expense.setCategory(
                resultSet.getString(
                        "category"
                )
        );

        expense.setDescription(
                resultSet.getString(
                        "description"
                )
        );

        expense.setAmount(
                resultSet.getDouble(
                        "amount"
                )
        );

        expense.setExpenseDate(
                LocalDate.parse(
                        resultSet.getString(
                                "expense_date"
                        )
                )
        );

        expense.setCreatedAt(
                LocalDateTime.parse(
                        resultSet.getString(
                                "created_at"
                        )
                )
        );

        return expense;
    }
}