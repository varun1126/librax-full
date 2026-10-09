package com.librax;

import com.librax.ui.LoginFrame;
import com.librax.ui.theme.AppTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 *
 * <p>Wires the {@link AppContext} composition root (config, database
 * schema init, repositories, services), installs the LibraX design system
 * over FlatLaf via {@link AppTheme}, and opens {@link LoginFrame} — which
 * itself routes to first-run administrator setup when no users exist yet
 * (README.md §14).</p>
 */
public final class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private App() {
        // Entry point only; not instantiable.
    }

    public static void main(String[] args) {
        log.info("Starting LibraX...");

        AppContext ctx = new AppContext();

        log.info("Library name: {}", ctx.appConfig.getLibraryName());
        log.info("Database file: {}", ctx.databaseConfig.getDatabaseFile());
        log.info("Google Books API base URL: {}", ctx.apiConfig.getBaseUrl());
        log.info("Google Books API key configured: {}", ctx.apiConfig.getApiKey().isPresent());

        AppTheme.install(ctx.appConfig.getUiTheme());

        SwingUtilities.invokeLater(() -> new LoginFrame(ctx).setVisible(true));
    }
}
