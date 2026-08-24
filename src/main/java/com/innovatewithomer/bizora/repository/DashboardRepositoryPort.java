package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.dashboard.DashboardSummary;
import com.innovatewithomer.bizora.model.dashboard.LowStockProduct;
import com.innovatewithomer.bizora.model.dashboard.RecentSale;

import java.util.List;

public interface DashboardRepositoryPort {

    DashboardSummary getSummary();

    List<RecentSale> findRecentSales(
            int limit
    );

    List<LowStockProduct> findLowStockProducts(
            int limit
    );
}