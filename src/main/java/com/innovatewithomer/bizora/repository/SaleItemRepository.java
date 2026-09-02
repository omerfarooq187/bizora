package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.SaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleItemRepository
        implements SaleItemRepositoryPort {

    @Override
    public void save(
            Connection connection,
            SaleItem item
    ) throws SQLException {

        String sql = """
                INSERT INTO sale_items (
                    sale_id,
                    product_id,
                    quantity,
                    unit_price,
                    cost_price,
                    discount,
                    subtotal
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    item.getSaleId()
            );

            statement.setLong(
                    2,
                    item.getProductId()
            );

            statement.setDouble(
                    3,
                    item.getQuantity()
            );

            statement.setDouble(
                    4,
                    item.getUnitPrice()
            );

            statement.setDouble(
                    5,
                    item.getCostPrice()
            );

            statement.setDouble(
                    6,
                    item.getDiscount()
            );

            statement.setDouble(
                    7,
                    item.getSubtotal()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {
                    item.setId(
                            keys.getLong(1)
                    );
                }
            }
        }
    }

    @Override
    public List<SaleItem> findBySaleId(
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
                    "Failed to find sale items.",
                    e
            );
        }
    }

    @Override
    public List<SaleItem> findBySaleId(
            Connection connection,
            Long saleId
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    sale_id,
                    product_id,
                    quantity,
                    unit_price,
                    cost_price,
                    discount,
                    subtotal
                FROM sale_items
                WHERE sale_id = ?
                ORDER BY id
                """;

        List<SaleItem> items =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, saleId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    items.add(
                            mapRow(resultSet)
                    );
                }
            }
        }

        return items;
    }

    private SaleItem mapRow(
            ResultSet resultSet
    ) throws SQLException {

        return new SaleItem(
                resultSet.getLong("id"),
                resultSet.getLong("sale_id"),
                resultSet.getLong("product_id"),
                resultSet.getDouble("quantity"),
                resultSet.getDouble("unit_price"),
                resultSet.getDouble("cost_price"),
                resultSet.getDouble("discount"),
                resultSet.getDouble("subtotal")
        );
    }
}
