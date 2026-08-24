package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Expense;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ExpenseRepositoryPort {

    Expense save(
            Connection connection,
            Expense expense
    ) throws SQLException;

    Expense findById(
            Long id
    );

    Expense findById(
            Connection connection,
            Long id
    ) throws SQLException;

    List<Expense> findAll();

    void delete(
            Connection connection,
            Long id
    ) throws SQLException;
}