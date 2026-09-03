package com.innovatewithomer.bizora.util;

import com.innovatewithomer.bizora.config.DatabaseConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("Bizora");
    private static Path logDirectory;
    private static boolean initialized;

    private AppLogger() {
    }

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        try {
            logDirectory = DatabaseConfig.getDataDirectory().resolve("logs");
            Files.createDirectories(logDirectory);
            FileHandler handler = new FileHandler(
                    logDirectory.resolve("bizora-%g.log").toString(),
                    1_000_000,
                    3,
                    true
            );
            handler.setFormatter(new SimpleFormatter());
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(handler);
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.WARNING, "File logging could not be initialized.", exception);
        }
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message, Throwable error) {
        LOGGER.log(Level.WARNING, message, error);
    }

    public static void error(String message, Throwable error) {
        LOGGER.log(Level.SEVERE, message, error);
    }

    public static Path getLogDirectory() {
        return logDirectory;
    }
}
