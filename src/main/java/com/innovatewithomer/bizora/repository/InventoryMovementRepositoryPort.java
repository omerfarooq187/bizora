package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.InventoryMovement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface InventoryMovementRepositoryPort {

    void save(InventoryMovement movement);

    void save(
            Connection connection,
            InventoryMovement movement
    ) throws SQLException;

    List<InventoryMovement> findByProductId(
            Long productId
    );

    List<InventoryMovement> findByProductId(
            Connection connection,
            Long productId
    ) throws SQLException;
}