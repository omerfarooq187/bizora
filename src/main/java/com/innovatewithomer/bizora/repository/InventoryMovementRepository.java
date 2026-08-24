package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.InventoryMovement;
import com.innovatewithomer.bizora.model.InventoryMovementType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryMovementRepository
        implements InventoryMovementRepositoryPort {

    @Override
    public List<InventoryMovement> findByProductId(
            Long productId
    ) {

        String sql = """
                SELECT
                    id,
                    product_id,
                    movement_type,
                    quantity,
                    reference_type,
                    reference_id,
                    note,
                    created_at
                FROM inventory_movements
                WHERE product_id = ?
                ORDER BY created_at DESC
                """;

        List<InventoryMovement> movements =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, productId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    movements.add(
                            mapRow(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find inventory movements.",
                    e
            );
        }

        return movements;
    }

    @Override
    public List<InventoryMovement> findByProductId(
            Connection connection,
            Long productId
    ) throws SQLException {

        String sql = """
            SELECT
                id,
                product_id,
                movement_type,
                quantity,
                reference_type,
                reference_id,
                note,
                created_at
            FROM inventory_movements
            WHERE product_id = ?
            ORDER BY created_at DESC
            """;

        List<InventoryMovement> movements =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    movements.add(
                            mapRow(resultSet)
                    );
                }
            }
        }

        return movements;
    }

    private InventoryMovement mapRow(
            ResultSet resultSet
    ) throws SQLException {

        InventoryMovement movement =
                new InventoryMovement();

        movement.setId(
                resultSet.getLong("id")
        );

        movement.setProductId(
                resultSet.getLong("product_id")
        );

        movement.setMovementType(
                InventoryMovementType.valueOf(
                        resultSet.getString(
                                "movement_type"
                        )
                )
        );

        movement.setQuantity(
                resultSet.getDouble("quantity")
        );

        movement.setReferenceType(
                resultSet.getString(
                        "reference_type"
                )
        );

        long referenceId =
                resultSet.getLong("reference_id");

        if (!resultSet.wasNull()) {
            movement.setReferenceId(referenceId);
        }

        movement.setNote(
                resultSet.getString("note")
        );

        movement.setCreatedAt(
                LocalDateTime.parse(
                        resultSet.getString(
                                "created_at"
                        )
                )
        );

        return movement;
    }

    public void save(
            InventoryMovement movement
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            save(connection, movement);

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save inventory movement.",
                    e
            );
        }
    }

    public void save(
            Connection connection,
            InventoryMovement movement
    ) throws SQLException {

        String sql = """
            INSERT INTO inventory_movements (
                product_id,
                movement_type,
                quantity,
                reference_type,
                reference_id,
                note,
                created_at
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
                    movement.getProductId()
            );

            statement.setString(
                    2,
                    movement.getMovementType().name()
            );

            statement.setDouble(
                    3,
                    movement.getQuantity()
            );

            statement.setString(
                    4,
                    movement.getReferenceType()
            );

            if (movement.getReferenceId() != null) {

                statement.setLong(
                        5,
                        movement.getReferenceId()
                );

            } else {

                statement.setNull(
                        5,
                        Types.INTEGER
                );
            }

            statement.setString(
                    6,
                    movement.getNote()
            );

            statement.setString(
                    7,
                    movement.getCreatedAt().toString()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    movement.setId(
                            keys.getLong(1)
                    );
                }
            }
        }
    }
}