package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository
        implements CustomerRepositoryPort {

    @Override
    public Customer save(Customer customer) {

        String sql = """
                INSERT INTO customers
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
                    customer.getName()
            );

            statement.setString(
                    2,
                    customer.getPhone()
            );

            statement.setString(
                    3,
                    customer.getEmail()
            );

            statement.setString(
                    4,
                    customer.getAddress()
            );

            statement.setString(
                    5,
                    customer.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    customer.setId(
                            keys.getLong(1)
                    );
                }
            }

            return customer;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save customer.",
                    e
            );
        }
    }

    @Override
    public List<Customer> findAll() {

        String sql = """
                SELECT
                    id,
                    name,
                    phone,
                    email,
                    address,
                    created_at
                FROM customers
                ORDER BY id DESC
                """;

        List<Customer> customers =
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

                customers.add(
                        mapRow(resultSet)
                );
            }

            return customers;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load customers.",
                    e
            );
        }
    }

    @Override
    public Customer findById(Long id) {

        try (
                Connection connection =
                        DatabaseManager.getConnection()
        ) {

            return findById(
                    connection,
                    id
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find customer.",
                    e
            );
        }
    }

    @Override
    public Customer findById(
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
                FROM customers
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

                if (resultSet.next()) {

                    return mapRow(resultSet);
                }

                return null;
            }
        }
    }

    @Override
    public void update(Customer customer) {

        String sql = """
                UPDATE customers
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
                    customer.getName()
            );

            statement.setString(
                    2,
                    customer.getPhone()
            );

            statement.setString(
                    3,
                    customer.getEmail()
            );

            statement.setString(
                    4,
                    customer.getAddress()
            );

            statement.setLong(
                    5,
                    customer.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update customer.",
                    e
            );
        }
    }

    @Override
    public void delete(Long id) {

        String sql = """
                DELETE FROM customers
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    id
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete customer.",
                    e
            );
        }
    }

    private Customer mapRow(
            ResultSet resultSet
    ) throws SQLException {

        return new Customer(
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