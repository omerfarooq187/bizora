package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository implements ProductRepositoryPort{

    public Product save(Product product) {

        String sql = """
                INSERT INTO products
                (name, sku, selling_price, purchase_price, stock_quantity, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getSku());
            statement.setDouble(3, product.getSellingPrice());
            statement.setDouble(4, product.getPurchasePrice());
            statement.setDouble(5, product.getStockQuantity());
            statement.setString(
                    6,
                    product.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }

            return product;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save product.",
                    e
            );
        }
    }

    public List<Product> findAll() {

        String sql = """
                SELECT
                    id,
                    name,
                    sku,
                    selling_price,
                    purchase_price,
                    stock_quantity,
                    created_at
                FROM products
                ORDER BY id DESC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                products.add(mapRow(resultSet));
            }

            return products;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load products.",
                    e
            );
        }
    }

    @Override
    public Product findById(Long id) {

        String sql = """
            SELECT
                id,
                name,
                sku,
                selling_price,
                purchase_price,
                stock_quantity,
                created_at
            FROM products
            WHERE id = ?
            """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find product.",
                    e
            );
        }
    }

    public Product findById(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
            SELECT
                id,
                name,
                sku,
                selling_price,
                purchase_price,
                stock_quantity,
                created_at
            FROM products
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }

                return null;
            }
        }
    }

    public void update(Product product) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            update(connection, product);

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update product.",
                    e
            );
        }
    }


    public void update(
            Connection connection,
            Product product
    ) throws SQLException {

        String sql = """
            UPDATE products
            SET
                name = ?,
                sku = ?,
                selling_price = ?,
                purchase_price = ?,
                stock_quantity = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    product.getName()
            );

            statement.setString(
                    2,
                    product.getSku()
            );

            statement.setDouble(
                    3,
                    product.getSellingPrice()
            );

            statement.setDouble(
                    4,
                    product.getPurchasePrice()
            );

            statement.setDouble(
                    5,
                    product.getStockQuantity()
            );

            statement.setLong(
                    6,
                    product.getId()
            );

            statement.executeUpdate();
        }
    }

    public void delete(Long id) {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete product.",
                    e
            );
        }
    }

    private Product mapRow(ResultSet resultSet)
            throws SQLException {

        return new Product(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("sku"),
                resultSet.getDouble("selling_price"),
                resultSet.getDouble("purchase_price"),
                resultSet.getDouble("stock_quantity"),
                LocalDateTime.parse(
                        resultSet.getString("created_at")
                )
        );
    }

    @Override
    public void updateStock(
            Connection connection,
            Long productId,
            double stockQuantity
    ) {

        String sql = """
            UPDATE products
            SET stock_quantity = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setDouble(1, stockQuantity);
            statement.setLong(2, productId);

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException(
                        "Product not found: " + productId
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update product stock.",
                    e
            );
        }
    }
}