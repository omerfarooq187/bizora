package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.Customer;
import com.innovatewithomer.bizora.repository.CustomerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    private CustomerService customerService;
    private FakeCustomerRepository customerRepository;

    @BeforeEach
    void setUp() {

        customerRepository =
                new FakeCustomerRepository();

        customerService =
                new CustomerService(
                        customerRepository
                );
    }

    @Test
    void shouldCreateCustomer() {

        Customer customer =
                new Customer(
                        "Ali Khan",
                        "03001234567",
                        "ali@example.com",
                        "Islamabad"
                );

        Customer saved =
                customerService.createCustomer(customer);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertEquals(
                "Ali Khan",
                saved.getName()
        );

        assertEquals(
                "03001234567",
                saved.getPhone()
        );
    }

    @Test
    void shouldGetCustomerById() {

        Customer customer =
                customerService.createCustomer(
                        new Customer(
                                "Ali Khan",
                                "03001234567",
                                "ali@example.com",
                                "Islamabad"
                        )
                );

        Customer found =
                customerService.getCustomer(
                        customer.getId()
                );

        assertNotNull(found);

        assertEquals(
                customer.getId(),
                found.getId()
        );

        assertEquals(
                "Ali Khan",
                found.getName()
        );
    }

    @Test
    void shouldGetAllCustomers() {

        customerService.createCustomer(
                new Customer(
                        "Ali",
                        "03001111111",
                        null,
                        "Islamabad"
                )
        );

        customerService.createCustomer(
                new Customer(
                        "Ahmed",
                        "03002222222",
                        null,
                        "Rawalpindi"
                )
        );

        List<Customer> customers =
                customerService.getAllCustomers();

        assertEquals(
                2,
                customers.size()
        );
    }

    @Test
    void shouldUpdateCustomer() {

        Customer customer =
                customerService.createCustomer(
                        new Customer(
                                "Ali",
                                "03001111111",
                                null,
                                "Islamabad"
                        )
                );

        customer.setName("Ali Khan");
        customer.setPhone("03009999999");

        customerService.updateCustomer(customer);

        Customer updated =
                customerService.getCustomer(
                        customer.getId()
                );

        assertEquals(
                "Ali Khan",
                updated.getName()
        );

        assertEquals(
                "03009999999",
                updated.getPhone()
        );
    }

    @Test
    void shouldDeleteCustomer() {

        Customer customer =
                customerService.createCustomer(
                        new Customer(
                                "Ali",
                                "03001111111",
                                null,
                                "Islamabad"
                        )
                );

        Long id = customer.getId();

        customerService.deleteCustomer(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.getCustomer(id)
        );
    }

    @Test
    void shouldRejectNullCustomer() {

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(null)
        );
    }

    @Test
    void shouldRejectBlankCustomerName() {

        Customer customer =
                new Customer(
                        "   ",
                        "03001111111",
                        null,
                        "Islamabad"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(customer)
        );
    }

    @Test
    void shouldRejectBlankCustomerPhone() {

        Customer customer =
                new Customer(
                        "Ali",
                        "   ",
                        null,
                        "Islamabad"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(customer)
        );
    }

    @Test
    void shouldRejectBlankCustomerEmail() {

        Customer customer =
                new Customer(
                        "Ali",
                        null,
                        "   ",
                        "Islamabad"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(customer)
        );
    }

    @Test
    void shouldAllowNullPhoneAndEmail() {

        Customer customer =
                new Customer(
                        "Ali",
                        null,
                        null,
                        "Islamabad"
                );

        assertDoesNotThrow(
                () -> customerService.createCustomer(customer)
        );
    }

    @Test
    void shouldRejectNullCustomerIdWhenGettingCustomer() {

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.getCustomer(null)
        );
    }

    @Test
    void shouldRejectUnknownCustomer() {

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.getCustomer(999999L)
        );
    }

    @Test
    void shouldRejectNullCustomerWhenUpdating() {

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(null)
        );
    }

    @Test
    void shouldRejectCustomerWithoutIdWhenUpdating() {

        Customer customer =
                new Customer(
                        "Ali",
                        "03001111111",
                        null,
                        "Islamabad"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(customer)
        );
    }

    @Test
    void shouldRejectNullIdWhenDeleting() {

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.deleteCustomer(null)
        );
    }

    private static class FakeCustomerRepository
            implements CustomerRepositoryPort {

        private final List<Customer> customers =
                new ArrayList<>();

        private long nextId = 1;

        @Override
        public Customer save(Customer customer) {

            customer.setId(nextId++);

            customers.add(customer);

            return customer;
        }

        @Override
        public List<Customer> findAll() {

            return new ArrayList<>(customers);
        }

        @Override
        public Customer findById(Long id) {

            return customers.stream()
                    .filter(customer ->
                            customer.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Customer findById(Connection connection, Long id) throws SQLException {
            return findById(id);
        }

        @Override
        public void update(Customer customer) {

            for (int i = 0; i < customers.size(); i++) {

                if (customers.get(i)
                        .getId()
                        .equals(customer.getId())) {

                    customers.set(i, customer);
                    return;
                }
            }
        }

        @Override
        public void delete(Long id) {

            customers.removeIf(
                    customer ->
                            customer.getId().equals(id)
            );
        }
    }
}