package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.dashboard.DashboardSummary;
import com.innovatewithomer.bizora.model.dashboard.LowStockProduct;
import com.innovatewithomer.bizora.model.dashboard.RecentSale;
import com.innovatewithomer.bizora.repository.DashboardRepositoryPort;

import java.util.List;

public class DashboardService {

    private final DashboardRepositoryPort dashboardRepository;

    public DashboardService(
            DashboardRepositoryPort dashboardRepository
    ) {
        this.dashboardRepository =
                dashboardRepository;
    }

    public DashboardSummary getSummary() {

        return dashboardRepository.getSummary();
    }

    public List<RecentSale> getRecentSales(
            int limit
    ) {

        validateLimit(limit);

        return dashboardRepository
                .findRecentSales(limit);
    }

    public List<LowStockProduct> getLowStockProducts(
            int limit
    ) {

        validateLimit(limit);

        return dashboardRepository
                .findLowStockProducts(limit);
    }

    private void validateLimit(int limit) {

        if (limit <= 0) {

            throw new IllegalArgumentException(
                    "Limit must be greater than zero."
            );
        }
    }
}