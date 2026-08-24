package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.PurchaseItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
}