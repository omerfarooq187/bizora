package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    List<Customer> findAll();

    Customer findById(Long id);

    Customer findById(
            Connection connection,
            Long id
    ) throws SQLException;

    void update(Customer customer);

    void delete(Long id);
}