package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleRepository
        implements SaleRepositoryPort {

    @Override
    public Sale save(
            Connection connection,
            Sale sale
    ) throws SQLException {

        String sql = """
                INSERT INTO sales (
                    customer_id,
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    sale_status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            /*
             * customer_id is optional.
             *
             * NULL means this is a walk-in sale.
             */
            if (sale.getCustomerId() == null) {

                statement.setNull(
                        1,
                        Types.INTEGER
                );

            } else {

                statement.setLong(
                        1,
                        sale.getCustomerId()
                );
            }

            statement.setString(
                    2,
                    sale.getInvoiceNumber()
            );

            statement.setDouble(
                    3,
                    sale.getSubtotal()
            );

            statement.setDouble(
                    4,
                    sale.getDiscount()
            );

            statement.setDouble(
                    5,
                    sale.getTax()
            );

            statement.setDouble(
                    6,
                    sale.getTotal()
            );

            statement.setString(
                    7,
                    sale.getPaymentStatus().name()
            );

            statement.setString(
                    8,
                    sale.getSaleStatus().name()
            );

            statement.setString(
                    9,
                    sale.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    sale.setId(
                            keys.getLong(1)
                    );
                }
            }
        }

        return sale;
    }

    @Override
    public Sale findById(Long id) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findById(
                    connection,
                    id
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find sale.",
                    e
            );
        }
    }

    @Override
    public Sale findById(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    customer_id,
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    sale_status,
                    created_at
                FROM sales
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
    public void updateStatus(
            Connection connection,
            Long saleId,
            SaleStatus status
    ) throws SQLException {

        String sql = """
                UPDATE sales
                SET sale_status = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name()
            );

            statement.setLong(
                    2,
                    saleId
            );

            statement.executeUpdate();
        }
    }

    @Override
    public List<Sale> findAll() {

        String sql = """
                SELECT
                    id,
                    customer_id,
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    sale_status,
                    created_at
                FROM sales
                ORDER BY id DESC
                """;

        List<Sale> sales =
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

                sales.add(
                        mapRow(resultSet)
                );
            }

            return sales;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load sales.",
                    e
            );
        }
    }

    private Sale mapRow(
            ResultSet resultSet
    ) throws SQLException {

        Sale sale =
                new Sale();

        sale.setId(
                resultSet.getLong("id")
        );

        /*
         * customer_id is nullable.
         *
         * ResultSet.getLong() returns 0 when
         * the database value is NULL.
         *
         * wasNull() lets us correctly restore
         * Java's null value.
         */
        long customerId =
                resultSet.getLong("customer_id");

        if (resultSet.wasNull()) {

            sale.setCustomerId(null);

        } else {

            sale.setCustomerId(
                    customerId
            );
        }

        sale.setInvoiceNumber(
                resultSet.getString(
                        "invoice_number"
                )
        );

        sale.setSubtotal(
                resultSet.getDouble(
                        "subtotal"
                )
        );

        sale.setDiscount(
                resultSet.getDouble(
                        "discount"
                )
        );

        sale.setTax(
                resultSet.getDouble(
                        "tax"
                )
        );

        sale.setTotal(
                resultSet.getDouble(
                        "total"
                )
        );

        sale.setPaymentStatus(
                PaymentStatus.valueOf(
                        resultSet.getString(
                                "payment_status"
                        )
                )
        );

        sale.setSaleStatus(
                SaleStatus.valueOf(
                        resultSet.getString(
                                "sale_status"
                        )
                )
        );

        sale.setCreatedAt(
                LocalDateTime.parse(
                        resultSet.getString(
                                "created_at"
                        )
                )
        );

        return sale;
    }


    @Override
    public void updatePaymentStatus(
            Connection connection,
            Long saleId,
            PaymentStatus status
    ) throws SQLException {

        String sql = """
        UPDATE sales
        SET payment_status = ?
        WHERE id = ?
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name()
            );

            statement.setLong(
                    2,
                    saleId
            );

            statement.executeUpdate();
        }
    }
}