package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FakeProductRepository
        implements ProductRepositoryPort {

    private final List<Product> products =
            new ArrayList<>();

    private long nextId = 1;

    @Override
    public Product save(Product product) {

        product.setId(nextId++);

        products.add(product);

        return product;
    }

    @Override
    public List<Product> findAll() {

        return new ArrayList<>(products);
    }

    @Override
    public Product findById(Long id) {

        return products.stream()
                .filter(product ->
                        product.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Product findById(
            Connection connection,
            Long id
    ) throws SQLException {

        return findById(id);
    }

    @Override
    public void update(Product product) {

        for (int i = 0; i < products.size(); i++) {

            if (products.get(i)
                    .getId()
                    .equals(product.getId())) {

                products.set(i, product);

                return;
            }
        }
    }

    @Override
    public void update(
            Connection connection,
            Product product
    ) throws SQLException {

        update(product);
    }

    @Override
    public void updateStock(
            Connection connection,
            Long productId,
            double stockQuantity
    ) {

        Product product =
                findById(productId);

        if (product == null) {
            throw new RuntimeException(
                    "Product not found: " + productId
            );
        }

        product.setStockQuantity(stockQuantity);
    }

    @Override
    public void delete(Long id) {

        products.removeIf(
                product ->
                        product.getId().equals(id)
        );
    }
}