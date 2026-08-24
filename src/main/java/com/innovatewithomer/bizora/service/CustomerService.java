package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.Customer;
import com.innovatewithomer.bizora.repository.CustomerRepositoryPort;

import java.util.List;

public class CustomerService {

    private final CustomerRepositoryPort customerRepository;

    public CustomerService(
            CustomerRepositoryPort customerRepository
    ) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {

        validate(customer);

        return customerRepository.save(customer);
    }

    public Customer getCustomer(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        Customer customer =
                customerRepository.findById(id);

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer not found: " + id
            );
        }

        return customer;
    }

    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    public void updateCustomer(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null."
            );
        }

        if (customer.getId() == null) {
            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        validate(customer);

        customerRepository.update(customer);
    }

    public void deleteCustomer(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        customerRepository.delete(id);
    }

    private void validate(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null."
            );
        }

        if (customer.getName() == null ||
                customer.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name is required."
            );
        }

        if (customer.getPhone() != null &&
                customer.getPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer phone cannot be blank."
            );
        }

        if (customer.getEmail() != null &&
                customer.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Customer email cannot be blank."
            );
        }
    }
}