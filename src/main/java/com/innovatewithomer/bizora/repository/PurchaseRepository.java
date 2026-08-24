package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Purchase;
import com.innovatewithomer.bizora.model.PurchaseStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRepository
        implements PurchaseRepositoryPort {

    @Override
    public Purchase save(
            Connection connection,
            Purchase purchase
    ) throws SQLException {

        String sql = """
                INSERT INTO purchases
                (
                    supplier_id,
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    purchase_status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            // supplier_id
            if (purchase.getSupplierId() == null) {

                statement.setNull(
                        1,
                        Types.INTEGER
                );

            } else {

                statement.setLong(
                        1,
                        purchase.getSupplierId()
                );
            }

            // invoice_number
            statement.setString(
                    2,
                    purchase.getInvoiceNumber()
            );

            // subtotal
            statement.setDouble(
                    3,
                    purchase.getSubtotal()
            );

            // discount
            statement.setDouble(
                    4,
                    purchase.getDiscount()
            );

            // tax
            statement.setDouble(
                    5,
                    purchase.getTax()
            );

            // total
            statement.setDouble(
                    6,
                    purchase.getTotal()
            );

            // payment_status
            statement.setString(
                    7,
                    purchase.getPaymentStatus().name()
            );

            // purchase_status
            statement.setString(
                    8,
                    purchase.getPurchaseStatus().name()
            );

            // created_at
            statement.setString(
                    9,
                    purchase.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    purchase.setId(
                            keys.getLong(1)
                    );
                }
            }
        }

        return purchase;
    }

    @Override
    public Purchase findById(Long id) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findById(
                    connection,
                    id
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find purchase.",
                    e
            );
        }
    }

    @Override
    public Purchase findById(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    supplier_id,
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    purchase_status,
                    created_at
                FROM purchases
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
    public List<Purchase> findAll() {

        String sql = """
                SELECT
                    id,
                    supplier_id,
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    purchase_status,
                    created_at
                FROM purchases
                ORDER BY id DESC
                """;

        List<Purchase> purchases =
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

                purchases.add(
                        mapRow(resultSet)
                );
            }

            return purchases;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load purchases.",
                    e
            );
        }
    }

    private Purchase mapRow(
            ResultSet resultSet
    ) throws SQLException {

        Purchase purchase =
                new Purchase();

        purchase.setId(
                resultSet.getLong("id")
        );

        /*
         * SQLite returns 0 for getLong()
         * when the column is NULL.
         *
         * Since supplier_id is optional,
         * convert SQL NULL back to Java null.
         */
        long supplierId =
                resultSet.getLong("supplier_id");

        if (resultSet.wasNull()) {

            purchase.setSupplierId(null);

        } else {

            purchase.setSupplierId(
                    supplierId
            );
        }

        purchase.setInvoiceNumber(
                resultSet.getString(
                        "invoice_number"
                )
        );

        purchase.setSubtotal(
                resultSet.getDouble(
                        "subtotal"
                )
        );

        purchase.setDiscount(
                resultSet.getDouble(
                        "discount"
                )
        );

        purchase.setTax(
                resultSet.getDouble(
                        "tax"
                )
        );

        purchase.setTotal(
                resultSet.getDouble(
                        "total"
                )
        );

        purchase.setPaymentStatus(
                PaymentStatus.valueOf(
                        resultSet.getString(
                                "payment_status"
                        )
                )
        );

        purchase.setPurchaseStatus(
                PurchaseStatus.valueOf(
                        resultSet.getString(
                                "purchase_status"
                        )
                )
        );

        purchase.setCreatedAt(
                LocalDateTime.parse(
                        resultSet.getString(
                                "created_at"
                        )
                )
        );

        return purchase;
    }
}