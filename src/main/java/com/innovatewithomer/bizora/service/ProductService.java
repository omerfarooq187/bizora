package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.repository.ProductRepository;
import com.innovatewithomer.bizora.repository.ProductRepositoryPort;

import java.util.List;

public class ProductService {

    private final ProductRepositoryPort productRepository;


    public ProductService(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {

        validate(product);

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {

        return productRepository.findAll();
    }

    public Product getProduct(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        Product product =
                productRepository.findById(id);

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product not found."
            );
        }

        return product;
    }

    public void updateProduct(Product product) {

        if (product.getId() == null) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        validate(product);

        productRepository.update(product);
    }

    public void deleteProduct(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        productRepository.delete(id);
    }

    private void validate(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        if (product.getName() == null ||
                product.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Product name is required."
            );
        }

        if (product.getSellingPrice() < 0) {

            throw new IllegalArgumentException(
                    "Selling price cannot be negative."
            );
        }

        if (product.getPurchasePrice() < 0) {

            throw new IllegalArgumentException(
                    "Purchase price cannot be negative."
            );
        }

        if (product.getStockQuantity() < 0) {

            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative."
            );
        }
    }
}