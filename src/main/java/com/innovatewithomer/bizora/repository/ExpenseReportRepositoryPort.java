package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.report.ExpenseReport;

import java.time.LocalDate;

public interface ExpenseReportRepositoryPort {

    ExpenseReport getExpenseReport(
            LocalDate from,
            LocalDate to
    );
}