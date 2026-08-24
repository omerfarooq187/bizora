package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.SaleItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SaleItemRepositoryPort {

    void save(
            Connection connection,
            SaleItem item
    ) throws SQLException;

    List<SaleItem> findBySaleId(Long saleId);

    List<SaleItem> findBySaleId(
            Connection connection,
            Long saleId
    ) throws SQLException;

}