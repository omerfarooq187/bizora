package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentMethod;
import com.innovatewithomer.bizora.model.PaymentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository
        implements PaymentRepositoryPort {

    @Override
    public Payment save(
            Connection connection,
            Payment payment
    ) throws SQLException {

        String sql = """
                INSERT INTO payments (
                    sale_id,
                    amount,
                    payment_method,
                    reference,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    payment.getSaleId()
            );

            statement.setDouble(
                    2,
                    payment.getAmount()
            );

            statement.setString(
                    3,
                    payment.getPaymentMethod().name()
            );

            statement.setString(
                    4,
                    payment.getReference()
            );

            statement.setString(
                    5,
                    payment.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    payment.setId(
                            keys.getLong(1)
                    );
                }
            }
        }

        return payment;
    }

    @Override
    public Payment findById(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    sale_id,
                    amount,
                    payment_method,
                    reference,
                    created_at
                FROM payments
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
    public Payment findById(Long id) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findById(
                    connection,
                    id
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find payment.",
                    e
            );
        }
    }

    @Override
    public List<Payment> findBySaleId(
            Connection connection,
            Long saleId
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    sale_id,
                    amount,
                    payment_method,
                    reference,
                    created_at
                FROM payments
                WHERE sale_id = ?
                ORDER BY id ASC
                """;

        List<Payment> payments =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    saleId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    payments.add(
                            mapRow(resultSet)
                    );
                }
            }
        }

        return payments;
    }

    @Override
    public List<Payment> findBySaleId(
            Long saleId
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findBySaleId(
                    connection,
                    saleId
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load payments.",
                    e
            );
        }
    }

    private Payment mapRow(
            ResultSet resultSet
    ) throws SQLException {

        Payment payment =
                new Payment();

        payment.setId(
                resultSet.getLong("id")
        );

        payment.setSaleId(
                resultSet.getLong("sale_id")
        );

        payment.setAmount(
                resultSet.getDouble("amount")
        );

        payment.setPaymentMethod(
                PaymentMethod.valueOf(
                        resultSet.getString(
                                "payment_method"
                        )
                )
        );

        payment.setReference(
                resultSet.getString(
                        "reference"
                )
        );

        payment.setCreatedAt(
                LocalDateTime.parse(
                        resultSet.getString(
                                "created_at"
                        )
                )
        );

        return payment;
    }
}