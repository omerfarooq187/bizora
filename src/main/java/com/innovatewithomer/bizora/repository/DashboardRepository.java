package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.SaleStatus;
import com.innovatewithomer.bizora.model.dashboard.DashboardSummary;
import com.innovatewithomer.bizora.model.dashboard.LowStockProduct;
import com.innovatewithomer.bizora.model.dashboard.RecentSale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository
        implements DashboardRepositoryPort {

    @Override
    public DashboardSummary getSummary() {

        String sql = """
                SELECT

                    (
                        SELECT COALESCE(
                            SUM(total),
                            0
                        )
                        FROM sales
                        WHERE DATE(created_at) = DATE('now', 'localtime')
                        AND sale_status = 'COMPLETED'
                    ) AS today_revenue,

                    (
                        SELECT COUNT(*)
                        FROM sales
                        WHERE DATE(created_at) = DATE('now', 'localtime')
                        AND sale_status = 'COMPLETED'
                    ) AS today_sales_count,

                    (
                        SELECT COUNT(*)
                        FROM products
                    ) AS total_product_count,

                    (
                        SELECT COUNT(*)
                        FROM products
                        WHERE stock_quantity <= 5
                    ) AS low_stock_count,

                    (
                        SELECT COALESCE(
                            SUM(total),
                            0
                        )
                        FROM purchases
                        WHERE DATE(created_at) = DATE('now', 'localtime')
                        AND purchase_status != 'CANCELLED'
                    ) AS today_purchases,

                    (
                        SELECT COALESCE(
                            SUM(item.quantity * item.cost_price),
                            0
                        )
                        FROM sale_items item
                        JOIN sales sale ON sale.id = item.sale_id
                        WHERE DATE(sale.created_at) = DATE('now', 'localtime')
                        AND sale.sale_status = 'COMPLETED'
                    ) AS today_cogs,

                    (
                        SELECT COALESCE(
                            SUM(amount),
                            0
                        )
                        FROM expenses
                        WHERE expense_date = DATE('now', 'localtime')
                    ) AS today_expenses
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (!resultSet.next()) {

                return new DashboardSummary(
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                );
            }

            double revenue = resultSet.getDouble("today_revenue");
            double purchases = resultSet.getDouble("today_purchases");
            double costOfGoodsSold = resultSet.getDouble("today_cogs");
            double expenses = resultSet.getDouble("today_expenses");
            double profit = revenue - costOfGoodsSold - expenses;

            return new DashboardSummary(
                    revenue,
                    resultSet.getLong("today_sales_count"),
                    resultSet.getLong("total_product_count"),
                    resultSet.getLong("low_stock_count"),
                    purchases,
                    expenses,
                    profit
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load dashboard summary.",
                    e
            );
        }
    }

    @Override
    public List<RecentSale> findRecentSales(
            int limit
    ) {

        String sql = """
                SELECT
                    s.id,
                    s.invoice_number,
                    c.name AS customer_name,
                    s.total,
                    s.payment_status,
                    s.sale_status,
                    s.created_at
                FROM sales s
                LEFT JOIN customers c ON s.customer_id = c.id
                ORDER BY s.created_at DESC
                LIMIT ?
                """;

        List<RecentSale> sales =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, limit);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    sales.add(
                            mapRecentSale(
                                    resultSet
                            )
                    );
                }
            }

            return sales;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load recent sales.",
                    e
            );
        }
    }

    @Override
    public List<LowStockProduct> findLowStockProducts(
            int limit
    ) {

        String sql = """
                SELECT
                    id,
                    name,
                    sku,
                    stock_quantity
                FROM products
                WHERE stock_quantity <= 5
                ORDER BY stock_quantity ASC
                LIMIT ?
                """;

        List<LowStockProduct> products =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, limit);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    products.add(
                            mapLowStockProduct(
                                    resultSet
                            )
                    );
                }
            }

            return products;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load low-stock products.",
                    e
            );
        }
    }

    private RecentSale mapRecentSale(
            ResultSet resultSet
    ) throws SQLException {

        return new RecentSale(
                resultSet.getLong("id"),

                resultSet.getString(
                        "invoice_number"
                ),

                resultSet.getString("customer_name"),

                resultSet.getDouble(
                        "total"
                ),

                PaymentStatus.valueOf(
                        resultSet.getString(
                                "payment_status"
                        )
                ),

                SaleStatus.valueOf(
                        resultSet.getString(
                                "sale_status"
                        )
                ),

                parseDateTime(
                        resultSet.getString(
                                "created_at"
                        )
                )
        );
    }

    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        if (text.contains(" ")) {
            text = text.replace(" ", "T");
        }
        return LocalDateTime.parse(text);
    }

    private LowStockProduct mapLowStockProduct(
            ResultSet resultSet
    ) throws SQLException {

        return new LowStockProduct(
                resultSet.getLong("id"),

                resultSet.getString(
                        "name"
                ),

                resultSet.getString(
                        "sku"
                ),

                resultSet.getDouble(
                        "stock_quantity"
                )
        );
    }
}
