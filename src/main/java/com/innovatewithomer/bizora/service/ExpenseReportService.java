package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.report.ExpenseReport;
import com.innovatewithomer.bizora.repository.ExpenseReportRepositoryPort;

import java.time.LocalDate;

public class ExpenseReportService {

    private final ExpenseReportRepositoryPort expenseReportRepository;

    public ExpenseReportService(
            ExpenseReportRepositoryPort expenseReportRepository
    ) {

        if (expenseReportRepository == null) {
            throw new IllegalArgumentException(
                    "Expense report repository cannot be null."
            );
        }

        this.expenseReportRepository =
                expenseReportRepository;
    }

    public ExpenseReport getExpenseReport(
            LocalDate from,
            LocalDate to
    ) {

        validateDateRange(
                from,
                to
        );

        return expenseReportRepository.getExpenseReport(
                from,
                to
        );
    }

    private void validateDateRange(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null) {
            throw new IllegalArgumentException(
                    "From date cannot be null."
            );
        }

        if (to == null) {
            throw new IllegalArgumentException(
                    "To date cannot be null."
            );
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date."
            );
        }
    }
}