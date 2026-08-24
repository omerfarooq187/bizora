package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.Supplier;
import com.innovatewithomer.bizora.repository.SupplierRepositoryPort;

import java.util.List;

public class SupplierService {

    private final SupplierRepositoryPort supplierRepository;

    public SupplierService(
            SupplierRepositoryPort supplierRepository
    ) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier createSupplier(Supplier supplier) {

        validate(supplier);

        return supplierRepository.save(supplier);
    }

    public Supplier getSupplier(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Supplier ID is required."
            );
        }

        Supplier supplier =
                supplierRepository.findById(id);

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier not found: " + id
            );
        }

        return supplier;
    }

    public List<Supplier> getAllSuppliers() {

        return supplierRepository.findAll();
    }

    public void updateSupplier(Supplier supplier) {

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier cannot be null."
            );
        }

        if (supplier.getId() == null) {
            throw new IllegalArgumentException(
                    "Supplier ID is required."
            );
        }

        validate(supplier);

        supplierRepository.update(supplier);
    }

    public void deleteSupplier(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Supplier ID is required."
            );
        }

        supplierRepository.delete(id);
    }

    private void validate(Supplier supplier) {

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier cannot be null."
            );
        }

        if (supplier.getName() == null ||
                supplier.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Supplier name is required."
            );
        }

        if (supplier.getPhone() != null &&
                supplier.getPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Supplier phone cannot be blank."
            );
        }

        if (supplier.getEmail() != null &&
                supplier.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Supplier email cannot be blank."
            );
        }
    }
}