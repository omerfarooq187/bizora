package com.innovatewithomer.bizora.infrastructure;

import com.innovatewithomer.bizora.infrastructure.migration.MigrationRunner;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationRunnerTest {

    @Test
    void shouldCreateLatestSchemaAndRemainIdempotent() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            MigrationRunner runner = new MigrationRunner();
            runner.run(connection);
            runner.run(connection);

            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT COUNT(*) FROM schema_version")) {
                assertTrue(result.next());
                assertEquals(9, result.getInt(1));
            }

            boolean hasCostPrice = false;
            try (var statement = connection.createStatement();
                 var columns = statement.executeQuery("PRAGMA table_info(sale_items)")) {
                while (columns.next()) {
                    if ("cost_price".equals(columns.getString("name"))) {
                        hasCostPrice = true;
                    }
                }
            }
            assertTrue(hasCostPrice);
        }
    }
}
