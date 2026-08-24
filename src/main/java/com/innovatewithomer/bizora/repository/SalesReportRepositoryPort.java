package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.SalesReport;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public interface SalesReportRepositoryPort {

    SalesReport getSalesReport(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException;
}