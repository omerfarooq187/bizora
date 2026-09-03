package com.innovatewithomer.bizora.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    private static final String APP_NAME = "Bizora";

    public static Path getDataDirectory() {

        String os = System.getProperty("os.name").toLowerCase();

        Path dataDirectory;

        if (os.contains("win")) {

            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isBlank()) {
                dataDirectory = Paths.get(
                        System.getProperty("user.home"),
                        "AppData",
                        "Roaming",
                        APP_NAME
                );
            } else {
                dataDirectory = Paths.get(appData, APP_NAME);
            }

        } else {

            String home = System.getProperty("user.home");

            dataDirectory = Paths.get(
                    home,
                    ".local",
                    "share",
                    APP_NAME
            );
        }

        try {

            Files.createDirectories(dataDirectory);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to create Bizora data directory at " + dataDirectory + ".",
                    e
            );
        }

        return dataDirectory;
    }

    public static String getJdbcUrl() {

        return "jdbc:sqlite:" + getDatabasePath();
    }

    public static Path getDatabasePath() {

        return getDataDirectory().resolve("bizora.db");
    }
}
