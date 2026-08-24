package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.TransactionManager;
import com.innovatewithomer.bizora.model.*;
import com.innovatewithomer.bizora.repository.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SaleService {

    private final SaleRepositoryPort saleRepository;
    private final SaleItemRepositoryPort saleItemRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final PaymentRepositoryPort paymentRepository;

    public SaleService(
            SaleRepositoryPort saleRepository,
            SaleItemRepositoryPort saleItemRepository,
            ProductRepositoryPort productRepository,
            InventoryMovementRepositoryPort movementRepository,
            PaymentRepositoryPort paymentRepository
    ) {

        if (saleRepository == null) {
            throw new IllegalArgumentException(
                    "Sale repository cannot be null."
            );
        }

        if (saleItemRepository == null) {
            throw new IllegalArgumentException(
                    "Sale item repository cannot be null."
            );
        }

        if (productRepository == null) {
            throw new IllegalArgumentException(
                    "Product repository cannot be null."
            );
        }

        if (movementRepository == null) {
            throw new IllegalArgumentException(
                    "Inventory movement repository cannot be null."
            );
        }

        if (paymentRepository == null) {
            throw new IllegalArgumentException(
                    "Payment repository cannot be null."
            );
        }

        this.saleRepository =
                saleRepository;

        this.saleItemRepository =
                saleItemRepository;

        this.productRepository =
                productRepository;

        this.movementRepository =
                movementRepository;

        this.paymentRepository =
                paymentRepository;
    }

    public Sale createSale(
            Sale sale
    ) {

        validateSale(sale);

        sale.calculateTotals();

        HashMap<Long, Product> products =
                new HashMap<>();

        TransactionManager.execute(connection -> {

            /*
             * Validate every product and stock level
             * before making any database changes.
             */
            for (SaleItem item :
                    sale.getItems()) {

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

                if (product.getStockQuantity()
                        < item.getQuantity()) {

                    throw new IllegalArgumentException(
                            "Insufficient stock for product: "
                                    + product.getName()
                    );
                }

                products.put(
                        product.getId(),
                        product
                );
            }

            /*
             * Save sale header.
             */
            saleRepository.save(
                    connection,
                    sale
            );

            /*
             * Save items, reduce stock,
             * and create STOCK_OUT movements.
             */
            for (SaleItem item :
                    sale.getItems()) {

                item.setSaleId(
                        sale.getId()
                );

                saleItemRepository.save(
                        connection,
                        item
                );

                Product product =
                        products.get(
                                item.getProductId()
                        );

                double newStock =
                        product.getStockQuantity()
                                - item.getQuantity();

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
                                InventoryMovementType.STOCK_OUT,
                                item.getQuantity(),
                                "SALE",
                                sale.getId(),
                                "Sale "
                                        + sale.getInvoiceNumber()
                        );

                movementRepository.save(
                        connection,
                        movement
                );
            }
        });

        return sale;
    }

    public Sale getSale(
            Long id
    ) {

        Sale sale =
                saleRepository.findById(id);

        if (sale == null) {

            throw new IllegalArgumentException(
                    "Sale not found: " + id
            );
        }

        List<SaleItem> items =
                saleItemRepository.findBySaleId(id);

        sale.setItems(items);

        return sale;
    }

    public List<Sale> getAllSales() {

        return saleRepository.findAll();
    }

    public Sale cancelSale(
            Long saleId
    ) {

        return TransactionManager.executeReturning(
                connection -> {

                    Sale sale =
                            saleRepository.findById(
                                    connection,
                                    saleId
                            );

                    if (sale == null) {

                        throw new IllegalArgumentException(
                                "Sale not found: "
                                        + saleId
                        );
                    }

                    if (sale.getSaleStatus()
                            == SaleStatus.CANCELLED) {

                        throw new IllegalArgumentException(
                                "Sale is already cancelled: "
                                        + saleId
                        );
                    }

                    /*
                     * A sale with an existing payment
                     * cannot be cancelled.
                     *
                     * Payment reversal/refund will be
                     * handled separately later.
                     */
                    List<Payment> payments =
                            paymentRepository.findBySaleId(
                                    connection,
                                    saleId
                            );

                    if (!payments.isEmpty()) {

                        throw new IllegalArgumentException(
                                "Cannot cancel a sale that has payments: "
                                        + saleId
                        );
                    }

                    List<SaleItem> items =
                            saleItemRepository.findBySaleId(
                                    connection,
                                    saleId
                            );

                    if (items.isEmpty()) {

                        throw new IllegalArgumentException(
                                "Cannot cancel sale without items: "
                                        + saleId
                        );
                    }

                    /*
                     * Restore stock and create
                     * RETURN_IN movements.
                     */
                    for (SaleItem item : items) {

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

                        double newStock =
                                product.getStockQuantity()
                                        + item.getQuantity();

                        productRepository.updateStock(
                                connection,
                                product.getId(),
                                newStock
                        );

                        InventoryMovement movement =
                                new InventoryMovement(
                                        product.getId(),
                                        InventoryMovementType.RETURN_IN,
                                        item.getQuantity(),
                                        "SALE",
                                        sale.getId(),
                                        "Cancelled sale "
                                                + sale.getInvoiceNumber()
                                );

                        movementRepository.save(
                                connection,
                                movement
                        );
                    }

                    saleRepository.updateStatus(
                            connection,
                            sale.getId(),
                            SaleStatus.CANCELLED
                    );

                    sale.setSaleStatus(
                            SaleStatus.CANCELLED
                    );

                    sale.setItems(items);

                    return sale;
                }
        );
    }

    private void validateSale(
            Sale sale
    ) {

        if (sale == null) {

            throw new IllegalArgumentException(
                    "Sale cannot be null."
            );
        }

        if (sale.getInvoiceNumber() == null ||
                sale.getInvoiceNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number is required."
            );
        }

        if (sale.getItems() == null ||
                sale.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Sale must contain at least one item."
            );
        }

        if (sale.getDiscount() < 0) {

            throw new IllegalArgumentException(
                    "Discount cannot be negative."
            );
        }

        if (sale.getTax() < 0) {

            throw new IllegalArgumentException(
                    "Tax cannot be negative."
            );
        }

        Set<Long> productIds =
                new HashSet<>();

        for (SaleItem item :
                sale.getItems()) {

            if (item == null) {

                throw new IllegalArgumentException(
                        "Sale cannot contain a null item."
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
                        "Product cannot appear more than once in a sale."
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