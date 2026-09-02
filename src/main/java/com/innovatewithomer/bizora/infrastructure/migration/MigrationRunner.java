package com.innovatewithomer.bizora.infrastructure.migration;

import com.innovatewithomer.bizora.infrastructure.DatabaseMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MigrationRunner {

    private final List<Migration> migrations = List.of(
            new V1__CreateProductsTable(),
            new V2__CreateInventoryMovementsTable(),
            new V3__CreateSalesTables(),
            new V4__CreatePurchasesTables(),
            new V5__CreateCustomersAndSuppliersTables(),
            new V6__AddSupplierToPurchases(),
            new V7__CreatePayments(),
            new V8__CreateExpensesTable(),
            new V9__AddCostPriceToSaleItems()
    );

    public void run(Connection connection) throws SQLException {

        DatabaseMigration.createMigrationTable(connection);

        for (Migration migration : migrations) {

            if (!isApplied(connection, migration.version())) {
                applyMigration(connection, migration);
            }
        }
    }

    private void applyMigration(
            Connection connection,
            Migration migration
    ) throws SQLException {
        boolean managesTransaction = connection.getAutoCommit();

        if (managesTransaction) {
            connection.setAutoCommit(false);
        }

        try {
            migration.migrate(connection);
            recordMigration(connection, migration);

            if (managesTransaction) {
                connection.commit();
            }

            System.out.println(
                    "Migration V" + migration.version()
                            + " applied: " + migration.description()
            );
        } catch (SQLException | RuntimeException e) {
            if (managesTransaction) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }
            throw e;
        } finally {
            if (managesTransaction) {
                connection.setAutoCommit(true);
            }
        }
    }

    private boolean isApplied(
            Connection connection,
            int version
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM schema_version
                WHERE version = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, version);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    private void recordMigration(
            Connection connection,
            Migration migration
    ) throws SQLException {

        String sql = """
                INSERT INTO schema_version
                (version, description, applied_at)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, migration.version());
            statement.setString(2, migration.description());
            statement.setString(
                    3,
                    LocalDateTime.now().toString()
            );

            statement.executeUpdate();
        }
    }
}
