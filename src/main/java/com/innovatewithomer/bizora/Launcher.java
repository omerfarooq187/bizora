package com.innovatewithomer.bizora;

/**
 * Plain Java entry point used by native launchers.
 * Keeping this class separate from JavaFX Application makes classpath-based
 * jpackage bundles start reliably without a separately installed JavaFX SDK.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
