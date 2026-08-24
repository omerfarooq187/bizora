package com.innovatewithomer.bizora.config;

import com.innovatewithomer.bizora.repository.*;
import com.innovatewithomer.bizora.service.*;

public final class AppContext {

    private static final ProductRepository PRODUCT_REPOSITORY =
            new ProductRepository();

    private static final ProductService PRODUCT_SERVICE =
            new ProductService(PRODUCT_REPOSITORY);

    private static final DashboardRepository DASHBOARD_REPOSITORY =
            new DashboardRepository();

    private static final SaleRepository SALE_REPOSITORY =
            new SaleRepository();

    private static final SaleItemRepository  SALE_ITEM_REPOSITORY =
            new SaleItemRepository();

    private static final PaymentRepository PAYMENT_REPOSITORY =
            new PaymentRepository();

    private static final InventoryMovementRepository INVENTORY_MOVEMENT_REPOSITORY =
            new InventoryMovementRepository();

    private static final DashboardService DASHBOARD_SERVICE =
            new DashboardService(
                    DASHBOARD_REPOSITORY
            );

    private static final InventoryService INVENTORY_SERVICE =
            new InventoryService(
                    PRODUCT_REPOSITORY,
                    INVENTORY_MOVEMENT_REPOSITORY
            );

    private static final SaleService SALE_SERVICE =
            new SaleService(
                    SALE_REPOSITORY,
                    SALE_ITEM_REPOSITORY,
                    PRODUCT_REPOSITORY,
                    INVENTORY_MOVEMENT_REPOSITORY,
                    PAYMENT_REPOSITORY
            );

    private static final PaymentService PAYMENT_SERVICE =
            new PaymentService(
                    PAYMENT_REPOSITORY,
                    SALE_REPOSITORY
            );

    private AppContext() {
        // Prevent instantiation.
    }

    public static ProductService productService() {
        return PRODUCT_SERVICE;
    }

    public static DashboardService dashboardService() {
        return DASHBOARD_SERVICE;
    }

    public static InventoryService inventoryService() {
        return INVENTORY_SERVICE;
    }

    public static SaleService saleService() {
        return SALE_SERVICE;
    }

    public static PaymentService paymentService() {
        return PAYMENT_SERVICE;
    }
}