package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Supplier;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SupplierRepositoryPort {

    Supplier save(Supplier supplier);

    List<Supplier> findAll();

    Supplier findById(Long id);

    Supplier findById(
            Connection connection,
            Long id
    ) throws SQLException;

    void update(Supplier supplier);

    void delete(Long id);
}