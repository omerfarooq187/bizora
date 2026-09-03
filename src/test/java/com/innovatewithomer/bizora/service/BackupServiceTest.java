package com.innovatewithomer.bizora.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldCreateAndRestoreValidatedBackup() throws Exception {
        Path database = temporaryDirectory.resolve("bizora.db");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE products (id INTEGER PRIMARY KEY, name TEXT)");
            statement.execute("INSERT INTO products (id, name) VALUES (1, 'Original')");
        }

        BackupService service = new BackupService(database);
        Path backup = service.createBackup();
        assertTrue(Files.isRegularFile(backup));
        assertEquals(1, service.listBackups().size());

        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             var statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE products SET name = 'Changed' WHERE id = 1");
        }

        Path safetyBackup = service.restoreBackup(backup);
        assertTrue(Files.isRegularFile(safetyBackup));
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT name FROM products WHERE id = 1")) {
            assertTrue(result.next());
            assertEquals("Original", result.getString(1));
        }
    }

    @Test
    void shouldRejectFilesThatAreNotBizoraBackups() throws Exception {
        Path database = temporaryDirectory.resolve("bizora.db");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE products (id INTEGER PRIMARY KEY)");
        }
        Path invalid = temporaryDirectory.resolve("invalid.db");
        Files.writeString(invalid, "not a database");

        BackupService service = new BackupService(database);
        assertThrows(IllegalArgumentException.class, () -> service.restoreBackup(invalid));
    }
}
