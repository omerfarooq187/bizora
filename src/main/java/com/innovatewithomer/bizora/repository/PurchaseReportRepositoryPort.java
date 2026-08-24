package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.report.PurchaseReport;

import java.time.LocalDate;

public interface PurchaseReportRepositoryPort {

    PurchaseReport getPurchaseReport(
            LocalDate from,
            LocalDate to
    );
}