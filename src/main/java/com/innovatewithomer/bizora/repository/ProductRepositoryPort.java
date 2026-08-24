package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ProductRepositoryPort {
    Product save(Product product);

    List<Product> findAll();

    Product findById(Long id);

    Product findById(
            Connection connection,
            Long id
    ) throws SQLException;

    void update(Product product);

    void update(
            Connection connection,
            Product product
    ) throws SQLException;

    void updateStock(
            Connection connection,
            Long productId,
            double stockQuantity
    );

    void delete(Long id);
}