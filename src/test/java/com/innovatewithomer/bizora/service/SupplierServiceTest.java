package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.Supplier;
import com.innovatewithomer.bizora.repository.SupplierRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierServiceTest {

    private SupplierService supplierService;
    private FakeSupplierRepository supplierRepository;

    @BeforeEach
    void setUp() {

        supplierRepository =
                new FakeSupplierRepository();

        supplierService =
                new SupplierService(
                        supplierRepository
                );
    }

    @Test
    void shouldCreateSupplier() {

        Supplier supplier =
                new Supplier(
                        "ABC Traders",
                        "03001234567",
                        "abc@example.com",
                        "Rawalpindi"
                );

        Supplier saved =
                supplierService.createSupplier(supplier);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertEquals(
                "ABC Traders",
                saved.getName()
        );

        assertEquals(
                "03001234567",
                saved.getPhone()
        );
    }

    @Test
    void shouldGetSupplierById() {

        Supplier supplier =
                supplierService.createSupplier(
                        new Supplier(
                                "ABC Traders",
                                "03001234567",
                                null,
                                "Rawalpindi"
                        )
                );

        Supplier found =
                supplierService.getSupplier(
                        supplier.getId()
                );

        assertNotNull(found);

        assertEquals(
                supplier.getId(),
                found.getId()
        );

        assertEquals(
                "ABC Traders",
                found.getName()
        );
    }

    @Test
    void shouldGetAllSuppliers() {

        supplierService.createSupplier(
                new Supplier(
                        "ABC Traders",
                        "03001111111",
                        null,
                        "Rawalpindi"
                )
        );

        supplierService.createSupplier(
                new Supplier(
                        "XYZ Traders",
                        "03002222222",
                        null,
                        "Islamabad"
                )
        );

        List<Supplier> suppliers =
                supplierService.getAllSuppliers();

        assertEquals(
                2,
                suppliers.size()
        );
    }

    @Test
    void shouldUpdateSupplier() {

        Supplier supplier =
                supplierService.createSupplier(
                        new Supplier(
                                "ABC Traders",
                                "03001111111",
                                null,
                                "Rawalpindi"
                        )
                );

        supplier.setName("ABC Wholesale");
        supplier.setPhone("03009999999");

        supplierService.updateSupplier(supplier);

        Supplier updated =
                supplierService.getSupplier(
                        supplier.getId()
                );

        assertEquals(
                "ABC Wholesale",
                updated.getName()
        );

        assertEquals(
                "03009999999",
                updated.getPhone()
        );
    }

    @Test
    void shouldDeleteSupplier() {

        Supplier supplier =
                supplierService.createSupplier(
                        new Supplier(
                                "ABC Traders",
                                "03001111111",
                                null,
                                "Rawalpindi"
                        )
                );

        Long id = supplier.getId();

        supplierService.deleteSupplier(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.getSupplier(id)
        );
    }

    @Test
    void shouldRejectNullSupplier() {

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.createSupplier(null)
        );
    }

    @Test
    void shouldRejectBlankSupplierName() {

        Supplier supplier =
                new Supplier(
                        "   ",
                        "03001111111",
                        null,
                        "Rawalpindi"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.createSupplier(supplier)
        );
    }

    @Test
    void shouldRejectBlankSupplierPhone() {

        Supplier supplier =
                new Supplier(
                        "ABC Traders",
                        "   ",
                        null,
                        "Rawalpindi"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.createSupplier(supplier)
        );
    }

    @Test
    void shouldRejectBlankSupplierEmail() {

        Supplier supplier =
                new Supplier(
                        "ABC Traders",
                        null,
                        "   ",
                        "Rawalpindi"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.createSupplier(supplier)
        );
    }

    @Test
    void shouldAllowNullPhoneAndEmail() {

        Supplier supplier =
                new Supplier(
                        "ABC Traders",
                        null,
                        null,
                        "Rawalpindi"
                );

        assertDoesNotThrow(
                () -> supplierService.createSupplier(supplier)
        );
    }

    @Test
    void shouldRejectNullSupplierIdWhenGettingSupplier() {

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.getSupplier(null)
        );
    }

    @Test
    void shouldRejectUnknownSupplier() {

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.getSupplier(999999L)
        );
    }

    @Test
    void shouldRejectNullSupplierWhenUpdating() {

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.updateSupplier(null)
        );
    }

    @Test
    void shouldRejectSupplierWithoutIdWhenUpdating() {

        Supplier supplier =
                new Supplier(
                        "ABC Traders",
                        "03001111111",
                        null,
                        "Rawalpindi"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.updateSupplier(supplier)
        );
    }

    @Test
    void shouldRejectNullIdWhenDeleting() {

        assertThrows(
                IllegalArgumentException.class,
                () -> supplierService.deleteSupplier(null)
        );
    }

    private static class FakeSupplierRepository
            implements SupplierRepositoryPort {

        private final List<Supplier> suppliers =
                new ArrayList<>();

        private long nextId = 1;

        @Override
        public Supplier save(Supplier supplier) {

            supplier.setId(nextId++);

            suppliers.add(supplier);

            return supplier;
        }

        @Override
        public List<Supplier> findAll() {

            return new ArrayList<>(suppliers);
        }

        @Override
        public Supplier findById(Long id) {

            return suppliers.stream()
                    .filter(supplier ->
                            supplier.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Supplier findById(Connection connection, Long id) throws SQLException {
            return findById(id);
        }

        @Override
        public void update(Supplier supplier) {

            for (int i = 0; i < suppliers.size(); i++) {

                if (suppliers.get(i)
                        .getId()
                        .equals(supplier.getId())) {

                    suppliers.set(i, supplier);
                    return;
                }
            }
        }

        @Override
        public void delete(Long id) {

            suppliers.removeIf(
                    supplier ->
                            supplier.getId().equals(id)
            );
        }
    }
}