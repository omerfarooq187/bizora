package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.TransactionManager;
import com.innovatewithomer.bizora.model.*;
import com.innovatewithomer.bizora.repository.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurchaseService {

    private final PurchaseRepositoryPort purchaseRepository;
    private final PurchaseItemRepositoryPort purchaseItemRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final SupplierRepositoryPort supplierRepository;

    public PurchaseService(
            PurchaseRepositoryPort purchaseRepository,
            PurchaseItemRepositoryPort purchaseItemRepository,
            ProductRepositoryPort productRepository,
            InventoryMovementRepositoryPort movementRepository,
            SupplierRepositoryPort supplierRepository
    ) {
        this.purchaseRepository =
                purchaseRepository;

        this.purchaseItemRepository =
                purchaseItemRepository;

        this.productRepository =
                productRepository;

        this.movementRepository =
                movementRepository;

        this.supplierRepository =
                supplierRepository;
    }

    public Purchase createPurchase(
            Purchase purchase
    ) {

        validatePurchase(purchase);

        purchase.calculateTotals();

        /*
         * The purchase discount must not reduce
         * the purchase below zero.
         */
        if (purchase.getDiscount()
                > purchase.getSubtotal()) {

            throw new IllegalArgumentException(
                    "Discount cannot exceed purchase subtotal."
            );
        }

        Map<Long, Product> products =
                new HashMap<>();

        TransactionManager.execute(connection -> {

            /*
             * Validate every product before making
             * any database changes.
             */
            for (PurchaseItem item :
                    purchase.getItems()) {

                Product product =
                        productRepository.findById(
                                connection,
                                item.getProductId()
                        );

                if (product == null) {

                    throw new IllegalArgumentException(
                            "Product not found: "
                                    + item.getProductId()
                    );
                }

                products.put(
                        product.getId(),
                        product
                );
            }

            /*
             * Supplier is optional.
             *
             * If supplied, it must exist.
             */
            if (purchase.getSupplierId() != null) {

                Supplier supplier =
                        supplierRepository.findById(
                                connection,
                                purchase.getSupplierId()
                        );

                if (supplier == null) {

                    throw new IllegalArgumentException(
                            "Supplier not found: "
                                    + purchase.getSupplierId()
                    );
                }
            }

            /*
             * Save purchase header.
             */
            purchaseRepository.save(
                    connection,
                    purchase
            );

            /*
             * Save purchase items,
             * increase stock,
             * and create inventory movements.
             */
            for (PurchaseItem item :
                    purchase.getItems()) {

                item.setPurchaseId(
                        purchase.getId()
                );

                purchaseItemRepository.save(
                        connection,
                        item
                );

                Product product =
                        products.get(
                                item.getProductId()
                        );

                double newStock =
                        product.getStockQuantity()
                                + item.getQuantity();

                productRepository.updateStock(
                        connection,
                        product.getId(),
                        newStock
                );

                product.setStockQuantity(
                        newStock
                );

                InventoryMovement movement =
                        new InventoryMovement(
                                product.getId(),
                                InventoryMovementType.STOCK_IN,
                                item.getQuantity(),
                                "PURCHASE",
                                purchase.getId(),
                                "Purchase "
                                        + purchase
                                        .getInvoiceNumber()
                        );

                movementRepository.save(
                        connection,
                        movement
                );
            }
        });

        return purchase;
    }

    public Purchase getPurchase(
            Long id
    ) {

        Purchase purchase =
                purchaseRepository.findById(id);

        if (purchase == null) {

            throw new IllegalArgumentException(
                    "Purchase not found: " + id
            );
        }

        List<PurchaseItem> items =
                purchaseItemRepository
                        .findByPurchaseId(id);

        purchase.setItems(items);

        return purchase;
    }

    public List<Purchase> getAllPurchases() {

        return purchaseRepository.findAll();
    }

    private void validatePurchase(
            Purchase purchase
    ) {

        if (purchase == null) {

            throw new IllegalArgumentException(
                    "Purchase cannot be null."
            );
        }

        if (purchase.getInvoiceNumber() == null ||
                purchase.getInvoiceNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number is required."
            );
        }

        if (purchase.getItems() == null ||
                purchase.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Purchase must contain at least one item."
            );
        }

        if (purchase.getDiscount() < 0) {

            throw new IllegalArgumentException(
                    "Discount cannot be negative."
            );
        }

        if (purchase.getTax() < 0) {

            throw new IllegalArgumentException(
                    "Tax cannot be negative."
            );
        }

        Set<Long> productIds =
                new HashSet<>();

        for (PurchaseItem item :
                purchase.getItems()) {

            if (item == null) {

                throw new IllegalArgumentException(
                        "Purchase cannot contain a null item."
                );
            }

            if (item.getProductId() == null) {

                throw new IllegalArgumentException(
                        "Product ID is required."
                );
            }

            if (!productIds.add(
                    item.getProductId()
            )) {

                throw new IllegalArgumentException(
                        "Product cannot appear more than once in a purchase."
                );
            }

            if (item.getQuantity() <= 0) {

                throw new IllegalArgumentException(
                        "Item quantity must be greater than zero."
                );
            }

            if (item.getUnitPrice() < 0) {

                throw new IllegalArgumentException(
                        "Unit price cannot be negative."
                );
            }

            if (item.getDiscount() < 0) {

                throw new IllegalArgumentException(
                        "Item discount cannot be negative."
                );
            }

            double itemValue =
                    item.getQuantity()
                            * item.getUnitPrice();

            if (item.getDiscount()
                    > itemValue) {

                throw new IllegalArgumentException(
                        "Item discount cannot exceed item value."
                );
            }
        }
    }
}
