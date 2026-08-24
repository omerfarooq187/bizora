package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Purchase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PurchaseRepositoryPort {

    Purchase save(
            Connection connection,
            Purchase purchase
    ) throws SQLException;

    Purchase findById(Long id);

    Purchase findById(
            Connection connection,
            Long id
    ) throws SQLException;

    List<Purchase> findAll();
}