package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PurchaseItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PurchaseItemRepository
        implements PurchaseItemRepositoryPort {

    @Override
    public void save(
            Connection connection,
            PurchaseItem item
    ) throws SQLException {

        String sql = """
                INSERT INTO purchase_items (
                    purchase_id,
                    product_id,
                    quantity,
                    unit_price,
                    discount,
                    subtotal
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    item.getPurchaseId()
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
                    item.getDiscount()
            );

            statement.setDouble(
                    6,
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
    public List<PurchaseItem> findByPurchaseId(
            Long purchaseId
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findByPurchaseId(
                    connection,
                    purchaseId
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find purchase items.",
                    e
            );
        }
    }

    @Override
    public List<PurchaseItem> findByPurchaseId(
            Connection connection,
            Long purchaseId
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    purchase_id,
                    product_id,
                    quantity,
                    unit_price,
                    discount,
                    subtotal
                FROM purchase_items
                WHERE purchase_id = ?
                ORDER BY id
                """;

        List<PurchaseItem> items =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    purchaseId
            );

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

    @Override
    public Map<Long, Integer> countByPurchase() {
        String sql = "SELECT purchase_id, COUNT(*) AS item_count FROM purchase_items GROUP BY purchase_id";
        Map<Long, Integer> counts = new HashMap<>();
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                counts.put(resultSet.getLong("purchase_id"), resultSet.getInt("item_count"));
            }
            return counts;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to count purchase items.", exception);
        }
    }

    private PurchaseItem mapRow(
            ResultSet resultSet
    ) throws SQLException {

        return new PurchaseItem(
                resultSet.getLong("id"),
                resultSet.getLong("purchase_id"),
                resultSet.getLong("product_id"),
                resultSet.getDouble("quantity"),
                resultSet.getDouble("unit_price"),
                resultSet.getDouble("discount"),
                resultSet.getDouble("subtotal")
        );
    }
}
