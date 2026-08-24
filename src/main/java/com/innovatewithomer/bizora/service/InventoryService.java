package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.TransactionManager;
import com.innovatewithomer.bizora.model.InventoryMovement;
import com.innovatewithomer.bizora.model.InventoryMovementType;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.repository.InventoryMovementRepository;
import com.innovatewithomer.bizora.repository.ProductRepository;

import java.util.List;

public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryService(
            ProductRepository productRepository,
            InventoryMovementRepository movementRepository
    ) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
    }

    public void stockIn(
            Long productId,
            double quantity,
            String referenceType,
            Long referenceId,
            String note
    ) {

        validateQuantity(quantity);
        validateProductId(productId);
        validateNote(note);

        TransactionManager.execute(connection -> {

            Product product =
                    productRepository.findById(
                            connection,
                            productId
                    );

            if (product == null) {
                throw new IllegalArgumentException(
                        "Product not found: " + productId
                );
            }

            double newStock =
                    product.getStockQuantity() + quantity;

            productRepository.updateStock(
                    connection,
                    productId,
                    newStock
            );

            InventoryMovement movement =
                    new InventoryMovement(
                            productId,
                            InventoryMovementType.STOCK_IN,
                            quantity,
                            referenceType,
                            referenceId,
                            note
                    );

            movementRepository.save(
                    connection,
                    movement
            );
        });
    }

    public void stockOut(
            Long productId,
            double quantity,
            String referenceType,
            Long referenceId,
            String note
    ) {

        validateQuantity(quantity);
        validateProductId(productId);
        validateNote(note);

        TransactionManager.execute(connection -> {

            Product product =
                    productRepository.findById(
                            connection,
                            productId
                    );

            if (product == null) {
                throw new IllegalArgumentException(
                        "Product not found: " + productId
                );
            }

            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock."
                );
            }

            double newStock =
                    product.getStockQuantity() - quantity;

            productRepository.updateStock(
                    connection,
                    productId,
                    newStock
            );

            InventoryMovement movement =
                    new InventoryMovement(
                            productId,
                            InventoryMovementType.STOCK_OUT,
                            quantity,
                            referenceType,
                            referenceId,
                            note
                    );

            movementRepository.save(
                    connection,
                    movement
            );
        });
    }

    public List<InventoryMovement> getMovements(
            Long productId
    ) {

        return movementRepository
                .findByProductId(productId);
    }

    private void validateQuantity(
            double quantity
    ) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }
    }

    public void adjustStock(
            Long productId,
            double actualStock,
            String note
    ) {

        if (actualStock < 0) {
            throw new IllegalArgumentException(
                    "Actual stock cannot be negative."
            );
        }

        TransactionManager.execute(connection -> {

            Product product =
                    productRepository.findById(
                            connection,
                            productId
                    );

            if (product == null) {
                throw new IllegalArgumentException(
                        "Product not found: " + productId
                );
            }

            double currentStock =
                    product.getStockQuantity();

            double difference =
                    actualStock - currentStock;

            // Nothing changed → nothing to record.
            if (difference == 0) {
                return;
            }

            productRepository.updateStock(
                    connection,
                    productId,
                    actualStock
            );

            InventoryMovementType type;

            double quantity;

            if (difference > 0) {

                type =
                        InventoryMovementType.ADJUSTMENT_IN;

                quantity = difference;

            } else {

                type =
                        InventoryMovementType.ADJUSTMENT_OUT;

                quantity = Math.abs(difference);
            }

            InventoryMovement movement =
                    new InventoryMovement(
                            productId,
                            type,
                            quantity,
                            "ADJUSTMENT",
                            null,
                            note
                    );

            movementRepository.save(
                    connection,
                    movement
            );
        });
    }

    private void validateProductId(Long productId) {

        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }
    }

    private void validateNote(String note) {

        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException(
                    "Adjustment note is required."
            );
        }
    }
}