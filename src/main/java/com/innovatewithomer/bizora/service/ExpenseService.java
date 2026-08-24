package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.TransactionManager;
import com.innovatewithomer.bizora.model.Expense;
import com.innovatewithomer.bizora.repository.ExpenseRepositoryPort;

import java.util.List;

public class ExpenseService {

    private final ExpenseRepositoryPort expenseRepository;

    public ExpenseService(
            ExpenseRepositoryPort expenseRepository
    ) {
        this.expenseRepository =
                expenseRepository;
    }

    public Expense createExpense(
            Expense expense
    ) {

        validateExpense(expense);

        if (expense.getCreatedAt() == null) {
            throw new IllegalArgumentException(
                    "Created date is required."
            );
        }

        TransactionManager.execute(connection -> {

            expenseRepository.save(
                    connection,
                    expense
            );
        });

        return expense;
    }

    public Expense getExpense(
            Long id
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Expense ID cannot be null."
            );
        }

        Expense expense =
                expenseRepository.findById(id);

        if (expense == null) {
            throw new IllegalArgumentException(
                    "Expense not found: " + id
            );
        }

        return expense;
    }

    public List<Expense> getAllExpenses() {

        return expenseRepository.findAll();
    }

    public void deleteExpense(
            Long id
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Expense ID cannot be null."
            );
        }

        /*
         * Verify that the expense exists
         * before attempting deletion.
         */
        Expense existing =
                expenseRepository.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Expense not found: " + id
            );
        }

        TransactionManager.execute(connection -> {

            expenseRepository.delete(
                    connection,
                    id
            );
        });
    }

    private void validateExpense(
            Expense expense
    ) {

        if (expense == null) {
            throw new IllegalArgumentException(
                    "Expense cannot be null."
            );
        }

        if (expense.getCategory() == null ||
                expense.getCategory().isBlank()) {

            throw new IllegalArgumentException(
                    "Expense category is required."
            );
        }

        if (expense.getAmount() <= 0) {

            throw new IllegalArgumentException(
                    "Expense amount must be greater than zero."
            );
        }

        if (expense.getExpenseDate() == null) {

            throw new IllegalArgumentException(
                    "Expense date is required."
            );
        }
    }
}