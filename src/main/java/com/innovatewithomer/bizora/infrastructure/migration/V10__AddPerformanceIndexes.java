package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V10__AddPerformanceIndexes implements Migration {

    @Override
    public int version() {
        return 10;
    }

    @Override
    public String description() {
        return "Add indexes for frequently refreshed screens";
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sales_date_status ON sales(created_at, sale_status)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sales_customer ON sales(customer_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sale_items_sale ON sale_items(sale_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sale_items_product ON sale_items(product_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_purchases_date_status ON purchases(created_at, purchase_status)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_purchase_items_purchase ON purchase_items(purchase_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_purchase_items_product ON purchase_items(product_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_inventory_product_date ON inventory_movements(product_id, created_at)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_payments_sale ON payments(sale_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(expense_date)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_customers_name ON customers(name)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers(name)");
        }
    }
}
