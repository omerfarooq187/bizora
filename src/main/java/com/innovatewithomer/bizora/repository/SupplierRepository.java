package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Supplier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository
        implements SupplierRepositoryPort {

    @Override
    public Supplier save(Supplier supplier) {

        String sql = """
                INSERT INTO suppliers
                (name, phone, email, address, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    supplier.getName()
            );

            statement.setString(
                    2,
                    supplier.getPhone()
            );

            statement.setString(
                    3,
                    supplier.getEmail()
            );

            statement.setString(
                    4,
                    supplier.getAddress()
            );

            statement.setString(
                    5,
                    supplier.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    supplier.setId(
                            keys.getLong(1)
                    );
                }
            }

            return supplier;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save supplier.",
                    e
            );
        }
    }

    @Override
    public List<Supplier> findAll() {

        String sql = """
                SELECT
                    id,
                    name,
                    phone,
                    email,
                    address,
                    created_at
                FROM suppliers
                ORDER BY id DESC
                """;

        List<Supplier> suppliers =
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

                suppliers.add(
                        mapRow(resultSet)
                );
            }

            return suppliers;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load suppliers.",
                    e
            );
        }
    }

    @Override
    public Supplier findById(Long id) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return findById(connection, id);

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find supplier.",
                    e
            );
        }
    }

    @Override
    public Supplier findById(
            Connection connection,
            Long id
    ) throws SQLException {

        String sql = """
            SELECT
                id,
                name,
                phone,
                email,
                address,
                created_at
            FROM suppliers
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

    @Override
    public void update(Supplier supplier) {

        String sql = """
                UPDATE suppliers
                SET
                    name = ?,
                    phone = ?,
                    email = ?,
                    address = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    supplier.getName()
            );

            statement.setString(
                    2,
                    supplier.getPhone()
            );

            statement.setString(
                    3,
                    supplier.getEmail()
            );

            statement.setString(
                    4,
                    supplier.getAddress()
            );

            statement.setLong(
                    5,
                    supplier.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update supplier.",
                    e
            );
        }
    }

    @Override
    public void delete(Long id) {

        String sql = """
                DELETE FROM suppliers
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete supplier.",
                    e
            );
        }
    }

    private Supplier mapRow(
            ResultSet resultSet
    ) throws SQLException {

        return new Supplier(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("address"),
                LocalDateTime.parse(
                        resultSet.getString("created_at")
                )
        );
    }
}