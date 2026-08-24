package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SaleRepositoryPort {

    Sale save(
            Connection connection,
            Sale sale
    ) throws SQLException;

    Sale findById(Long id);

    Sale findById(
            Connection connection,
            Long id
    ) throws SQLException;

    void updateStatus(
            Connection connection,
            Long saleId,
            SaleStatus status
    ) throws SQLException;

    List<Sale> findAll();

    void updatePaymentStatus(
            Connection connection,
            Long saleId,
            PaymentStatus status
    ) throws SQLException;
}