package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.config.DatabaseConfig;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class BackupService {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final int MAX_AUTOMATIC_BACKUPS = 14;

    private final Path databasePath;
    private final Path backupDirectory;

    public BackupService() {
        this(DatabaseConfig.getDatabasePath());
    }

    public BackupService(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath().normalize();
        Path parent = this.databasePath.getParent();
        if (parent == null) throw new IllegalArgumentException("Database must have a parent directory.");
        this.backupDirectory = parent.resolve("backups");
    }

    public Path createBackup() {
        if (!Files.isRegularFile(databasePath)) {
            throw new IllegalStateException("The Bizora database does not exist yet.");
        }
        Path destination = null;
        try {
            Files.createDirectories(backupDirectory);
            destination = backupDirectory.resolve(
                    "bizora-backup-" + LocalDateTime.now().format(FILE_TIME) + ".db");
            String escapedDestination = destination.toString().replace("'", "''");
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                 Statement statement = connection.createStatement()) {
                statement.execute("VACUUM INTO '" + escapedDestination + "'");
            }
            validateBackup(destination);
            return destination;
        } catch (Exception exception) {
            // A failed VACUUM can leave an incomplete destination file behind.
            try { if (destination != null) Files.deleteIfExists(destination); }
            catch (IOException ignored) { }
            throw new RuntimeException("Could not create a database backup.", exception);
        }
    }

    public Path createDailyBackupIfNeeded() {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        boolean alreadyCreated = listBackups().stream()
                .anyMatch(path -> path.getFileName().toString().startsWith("bizora-backup-" + today));
        if (alreadyCreated || !Files.isRegularFile(databasePath)) return null;
        Path created = createBackup();
        pruneOldBackups();
        return created;
    }

    public Path restoreBackup(Path source) {
        Path normalized = source == null ? null : source.toAbsolutePath().normalize();
        if (normalized == null || !Files.isRegularFile(normalized)) {
            throw new IllegalArgumentException("Choose a valid Bizora backup file.");
        }
        validateBackup(normalized);
        Path safetyBackup = createBackup();
        try {
            Files.copy(normalized, databasePath, StandardCopyOption.REPLACE_EXISTING);
            validateBackup(databasePath);
            return safetyBackup;
        } catch (Exception exception) {
            try {
                Files.copy(safetyBackup, databasePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException restoreFailure) {
                exception.addSuppressed(restoreFailure);
            }
            throw new RuntimeException("Could not restore the selected backup.", exception);
        }
    }

    public List<Path> listBackups() {
        if (!Files.isDirectory(backupDirectory)) return List.of();
        try (var paths = Files.list(backupDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .sorted(Comparator.comparingLong(this::lastModified).reversed())
                    .toList();
        } catch (IOException exception) {
            throw new RuntimeException("Could not read the backup folder.", exception);
        }
    }

    public Path getBackupDirectory() {
        return backupDirectory;
    }

    private void validateBackup(Path path) {
        String uri = path.toUri().toString() + "?mode=ro";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + uri);
             Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
                if (!result.next() || !"ok".equalsIgnoreCase(result.getString(1))) {
                    throw new IllegalArgumentException("The selected file is not a healthy SQLite backup.");
                }
            }
            try (ResultSet result = statement.executeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='products'")) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new IllegalArgumentException("The selected file is not a Bizora backup.");
                }
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("The selected file is not a valid Bizora backup.", exception);
        }
    }

    private void pruneOldBackups() {
        List<Path> backups = listBackups();
        for (int index = MAX_AUTOMATIC_BACKUPS; index < backups.size(); index++) {
            try { Files.deleteIfExists(backups.get(index)); }
            catch (IOException ignored) { /* A backup should never block app startup. */ }
        }
    }

    private long lastModified(Path path) {
        try { return Files.getLastModifiedTime(path).toMillis(); }
        catch (IOException ignored) { return 0; }
    }
}
