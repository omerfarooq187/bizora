package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.PurchaseItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface PurchaseItemRepositoryPort {

    void save(
            Connection connection,
            PurchaseItem item
    ) throws SQLException;

    List<PurchaseItem> findByPurchaseId(
            Long purchaseId
    );

    List<PurchaseItem> findByPurchaseId(
            Connection connection,
            Long purchaseId
    ) throws SQLException;

    Map<Long, Integer> countByPurchase();
}
