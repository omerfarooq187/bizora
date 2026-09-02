package com.innovatewithomer.bizora.config;

import com.innovatewithomer.bizora.repository.*;
import com.innovatewithomer.bizora.service.*;

public final class AppContext {

    private static final ProductRepository PRODUCT_REPOSITORY =
            new ProductRepository();

    private static final ProductService PRODUCT_SERVICE =
            new ProductService(PRODUCT_REPOSITORY);

    private static final CustomerRepository CUSTOMER_REPOSITORY =
            new CustomerRepository();

    private static final CustomerService CUSTOMER_SERVICE =
            new CustomerService(CUSTOMER_REPOSITORY);

    private static final SupplierRepository SUPPLIER_REPOSITORY =
            new SupplierRepository();

    private static final SupplierService SUPPLIER_SERVICE =
            new SupplierService(SUPPLIER_REPOSITORY);

    private static final DashboardRepository DASHBOARD_REPOSITORY =
            new DashboardRepository();

    private static final SaleRepository SALE_REPOSITORY =
            new SaleRepository();

    private static final SaleItemRepository SALE_ITEM_REPOSITORY =
            new SaleItemRepository();

    private static final PurchaseRepository PURCHASE_REPOSITORY =
            new PurchaseRepository();

    private static final PurchaseItemRepository PURCHASE_ITEM_REPOSITORY =
            new PurchaseItemRepository();

    private static final PaymentRepository PAYMENT_REPOSITORY =
            new PaymentRepository();

    private static final InventoryMovementRepository INVENTORY_MOVEMENT_REPOSITORY =
            new InventoryMovementRepository();

    private static final ExpenseRepository EXPENSE_REPOSITORY =
            new ExpenseRepository();

    private static final SalesReportRepository SALES_REPORT_REPOSITORY =
            new SalesReportRepository();

    private static final PurchaseReportRepository PURCHASE_REPORT_REPOSITORY =
            new PurchaseReportRepository();

    private static final ExpenseReportRepository EXPENSE_REPORT_REPOSITORY =
            new ExpenseReportRepository();

    private static final FinancialSummaryRepository FINANCIAL_SUMMARY_REPOSITORY =
            new FinancialSummaryRepository();

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

    private static final PurchaseService PURCHASE_SERVICE =
            new PurchaseService(
                    PURCHASE_REPOSITORY,
                    PURCHASE_ITEM_REPOSITORY,
                    PRODUCT_REPOSITORY,
                    INVENTORY_MOVEMENT_REPOSITORY,
                    SUPPLIER_REPOSITORY
            );

    private static final PaymentService PAYMENT_SERVICE =
            new PaymentService(
                    PAYMENT_REPOSITORY,
                    SALE_REPOSITORY
            );

    private static final ExpenseService EXPENSE_SERVICE =
            new ExpenseService(
                    EXPENSE_REPOSITORY
            );

    private static final SalesReportService SALES_REPORT_SERVICE =
            new SalesReportService(
                    SALES_REPORT_REPOSITORY
            );

    private static final PurchaseReportService PURCHASE_REPORT_SERVICE =
            new PurchaseReportService(
                    PURCHASE_REPORT_REPOSITORY
            );

    private static final ExpenseReportService EXPENSE_REPORT_SERVICE =
            new ExpenseReportService(
                    EXPENSE_REPORT_REPOSITORY
            );

    private static final FinancialSummaryService FINANCIAL_SUMMARY_SERVICE =
            new FinancialSummaryService(
                    FINANCIAL_SUMMARY_REPOSITORY
            );

    private AppContext() {
        // Prevent instantiation.
    }

    public static ProductService productService() {
        return PRODUCT_SERVICE;
    }

    public static CustomerService customerService() {
        return CUSTOMER_SERVICE;
    }

    public static SupplierService supplierService() {
        return SUPPLIER_SERVICE;
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

    public static PurchaseService purchaseService() {
        return PURCHASE_SERVICE;
    }

    public static PaymentService paymentService() {
        return PAYMENT_SERVICE;
    }

    public static ExpenseService expenseService() {
        return EXPENSE_SERVICE;
    }

    public static SalesReportService salesReportService() {
        return SALES_REPORT_SERVICE;
    }

    public static PurchaseReportService purchaseReportService() {
        return PURCHASE_REPORT_SERVICE;
    }

    public static ExpenseReportService expenseReportService() {
        return EXPENSE_REPORT_SERVICE;
    }

    public static FinancialSummaryService financialSummaryService() {
        return FINANCIAL_SUMMARY_SERVICE;
    }
}