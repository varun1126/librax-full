package com.librax.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Installs and switches the LibraX design system.
 *
 * <p>All semantic color tokens (see {@link Theme}) are published into
 * {@link UIManager} under the {@code LibraX.*} namespace together with a
 * curated set of FlatLaf defaults, so every component — FlatLaf's or ours —
 * derives from the same palette. Switching themes re-installs the look and
 * feel and re-publishes the tokens (DESIGN.md §16: components never hardcode
 * colors).</p>
 */
public final class AppTheme {

    private static final Logger log = LoggerFactory.getLogger(AppTheme.class);

    /** Marks which palette is currently published (used by {@link Theme#isDark()}). */
    public static final String KEY_DARK = "LibraX.dark";

    private AppTheme() {
    }

    /**
     * Installs the palette named in configuration and publishes every token.
     *
     * @param themeName {@code "dark"} for the dark palette, anything else for light
     */
    public static void install(String themeName) {
        install("dark".equalsIgnoreCase(themeName));
    }

    /**
     * Installs the light or dark palette and publishes every design token.
     *
     * <p>Safe to call again at runtime to switch themes; call
     * {@code SwingUtilities.updateComponentTreeUI(...)} (or rebuild the shell)
     * afterwards so open windows repaint against the new tokens.</p>
     *
     * @param dark {@code true} to install the dark palette
     */
    public static void install(boolean dark) {
        setLookAndFeel(dark ? new FlatDarkLaf() : new FlatLightLaf(), dark);
        publishTokens(dark);
        publishFlatLafDefaults(dark);
        UIManager.put(KEY_DARK, dark);
        UIManager.put("defaultFont", new FontUIResource(baseFamily(), Font.PLAIN, 13));
    }

    /**
     * Convenience switcher used by the appearance controls: re-installs the
     * opposite palette.
     *
     * @param dark {@code true} to switch to the dark palette
     */
    public static void setDark(boolean dark) {
        install(dark);
    }

    private static void setLookAndFeel(LookAndFeel laf, boolean dark) {
        try {
            UIManager.setLookAndFeel(laf);
            FlatLaf.updateUI();
            log.info("Applied FlatLaf theme: {}", dark ? "dark" : "light");
        } catch (UnsupportedLookAndFeelException e) {
            // Never fail the application over a theming problem; Swing falls
            // back to the default look and feel while tokens still publish.
            log.warn("Failed to initialize FlatLaf theme, falling back to default LAF", e);
        }
    }

    /** Chooses the interface typeface: Inter when installed, else Segoe UI, else system default. */
    static String baseFamily() {
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String preferred : new String[]{"Inter", "Segoe UI"}) {
            for (String family : families) {
                if (family.equalsIgnoreCase(preferred)) {
                    return family;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    /** Publishes every semantic color token for the selected palette. */
    private static void publishTokens(boolean dark) {
        if (dark) {
            put("LibraX.workspace", "#16181C");
            put("LibraX.surface", "#1D2024");
            put("LibraX.surfaceAlt", "#22262B");
            put("LibraX.border", "#2C3138");
            put("LibraX.borderStrong", "#3A404A");

            put("LibraX.text.primary", "#E7EAEE");
            put("LibraX.text.secondary", "#A2A9B4");
            put("LibraX.text.muted", "#767E8A");

            put("LibraX.accent", "#3B82F6");
            put("LibraX.accent.hover", "#60A5FA");
            put("LibraX.accent.subtle", "#1D2A44");
            put("LibraX.accent.text", "#8AB4FF");
            put("LibraX.onAccent", "#FFFFFF");

            put("LibraX.success", "#22C55E");
            put("LibraX.success.subtle", "#16281D");
            put("LibraX.success.text", "#5FD98D");
            put("LibraX.warning", "#F59E0B");
            put("LibraX.warning.subtle", "#2E2312");
            put("LibraX.warning.text", "#F5B544");
            put("LibraX.danger", "#EF4444");
            put("LibraX.danger.subtle", "#301A1C");
            put("LibraX.danger.text", "#F58585");
            put("LibraX.info", "#22D3EE");
            put("LibraX.info.subtle", "#122A30");
            put("LibraX.info.text", "#6FD8EA");
            put("LibraX.neutral.subtle", "#262B31");
            put("LibraX.neutral.text", "#A2A9B4");

            put("LibraX.sidebar.background", "#131519");
            put("LibraX.sidebar.border", "#0D0F12");
            put("LibraX.sidebar.hover", "#1E2127");
            put("LibraX.sidebar.selected", "#272C34");
            put("LibraX.sidebar.text", "#A6ADB8");
            put("LibraX.sidebar.text.selected", "#F2F4F7");
            put("LibraX.sidebar.text.muted", "#6B727D");

            put("LibraX.selection", "#24375C");
            put("LibraX.shadow", "#000000");
        } else {
            put("LibraX.workspace", "#F6F7F9");
            put("LibraX.surface", "#FFFFFF");
            put("LibraX.surfaceAlt", "#FAFAFB");
            put("LibraX.border", "#E4E6EA");
            put("LibraX.borderStrong", "#D3D7DD");

            put("LibraX.text.primary", "#1F2328");
            put("LibraX.text.secondary", "#59606B");
            put("LibraX.text.muted", "#8B929C");

            put("LibraX.accent", "#2563EB");
            put("LibraX.accent.hover", "#1D4ED8");
            put("LibraX.accent.subtle", "#EBF1FE");
            put("LibraX.accent.text", "#1D4ED8");
            put("LibraX.onAccent", "#FFFFFF");

            put("LibraX.success", "#16A34A");
            put("LibraX.success.subtle", "#E8F6ED");
            put("LibraX.success.text", "#157A3E");
            put("LibraX.warning", "#D97706");
            put("LibraX.warning.subtle", "#FCF2E4");
            put("LibraX.warning.text", "#9A5B06");
            put("LibraX.danger", "#DC2626");
            put("LibraX.danger.subtle", "#FCEDED");
            put("LibraX.danger.text", "#B42323");
            put("LibraX.info", "#0891B2");
            put("LibraX.info.subtle", "#E4F3F7");
            put("LibraX.info.text", "#0B6C85");
            put("LibraX.neutral.subtle", "#EFF1F4");
            put("LibraX.neutral.text", "#59606B");

            put("LibraX.sidebar.background", "#1B1D21");
            put("LibraX.sidebar.border", "#111316");
            put("LibraX.sidebar.hover", "#26292E");
            put("LibraX.sidebar.selected", "#2E323A");
            put("LibraX.sidebar.text", "#9BA2AC");
            put("LibraX.sidebar.text.selected", "#F2F4F7");
            put("LibraX.sidebar.text.muted", "#6B727D");

            put("LibraX.selection", "#E7EEFD");
            put("LibraX.shadow", "#000000");
        }

        // Expose the core tokens to FlatLaf so stock components (labels,
        // tables, popups, dialogs) follow the same palette automatically.
        color("Panel.background", "LibraX.workspace");
        color("OptionPane.background", "LibraX.surface");
        color("Label.foreground", "LibraX.text.primary");
        color("Label.disabledForeground", "LibraX.text.muted");
        color("Separator.foreground", "LibraX.border");
        color("TitledBorder.titleColor", "LibraX.text.primary");
        color("CheckBox.interpolation", "LibraX.accent");
    }

    private static void publishFlatLafDefaults(boolean dark) {
        // Restrained geometry: small, consistent corner radii only.
        UIManager.put("Component.arc", 4);
        UIManager.put("Button.arc", 4);
        UIManager.put("TextComponent.arc", 4);
        UIManager.put("ProgressBar.arc", 4);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.innerFocusWidth", 1);

        // Buttons
        UIManager.put("Button.background", UIManager.get("LibraX.surface"));
        UIManager.put("Button.default.background", colorOf("LibraX.accent"));
        UIManager.put("Button.default.foreground", colorOf("LibraX.onAccent"));
        UIManager.put("Button.default.mouseOverBackground", colorOf("LibraX.accent.hover"));
        UIManager.put("Button.default.mousePressedBackground", colorOf("LibraX.accent.hover"));
        UIManager.put("Button.focusedBackground", UIManager.get("Button.background"));

        // Inputs
        UIManager.put("TextField.background", UIManager.get("LibraX.surface"));
        UIManager.put("PasswordField.background", UIManager.get("LibraX.surface"));
        UIManager.put("TextArea.background", UIManager.get("LibraX.surface"));
        UIManager.put("ComboBox.background", UIManager.get("LibraX.surface"));
        UIManager.put("FormattedTextField.background", UIManager.get("LibraX.surface"));
        UIManager.put("InputButton.hoverBackground", UIManager.get("LibraX.surfaceAlt"));

        // Tables — dense, quiet, horizontal rules only.
        UIManager.put("Table.background", UIManager.get("LibraX.surface"));
        UIManager.put("Table.foreground", colorOf("LibraX.text.primary"));
        UIManager.put("Table.alternateRowColor", UIManager.get("LibraX.surfaceAlt"));
        UIManager.put("Table.selectionBackground", colorOf("LibraX.selection"));
        UIManager.put("Table.selectionForeground", colorOf("LibraX.text.primary"));
        UIManager.put("Table.focusCellBackground", UIManager.get("Table.selectionBackground"));
        UIManager.put("Table.focusCellForeground", UIManager.get("Table.selectionForeground"));
        UIManager.put("Table.gridColor", colorOf("LibraX.border"));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("TableHeader.background", UIManager.get("LibraX.surface"));
        UIManager.put("TableHeader.foreground", colorOf("LibraX.text.secondary"));
        UIManager.put("TableHeader.separatorColor", colorOf("LibraX.border"));
        UIManager.put("TableHeader.bottomSeparatorColor", colorOf("LibraX.borderStrong"));
        UIManager.put("TableHeader.borderColor", colorOf("LibraX.border"));

        // Popups / menus / toolbars
        UIManager.put("PopupMenu.background", UIManager.get("LibraX.surface"));
        UIManager.put("PopupMenu.borderColor", colorOf("LibraX.borderStrong"));
        UIManager.put("MenuItem.selectionBackground", colorOf("LibraX.selection"));
        UIManager.put("MenuItem.selectionForeground", colorOf("LibraX.text.primary"));
        UIManager.put("Menu.selectionBackground", colorOf("LibraX.selection"));
        UIManager.put("ToolButton.selectionBackground", colorOf("LibraX.selection"));
        UIManager.put("PopupMenu.dropShadowColor", new Color(0, 0, 0, dark ? 140 : 40));

        // Scroll bars stay unobtrusive.
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbColor", dark ? new Color(0x3A, 0x40, 0x4A) : new Color(0xC9, 0xCD, 0xD4));
        UIManager.put("ScrollBar.thumbDarkShadowColor", UIManager.get("ScrollBar.thumbColor"));
        UIManager.put("ScrollBar.thumbHighlightColor", UIManager.get("ScrollBar.thumbColor"));
        UIManager.put("ScrollBar.thumbShadowColor", UIManager.get("ScrollBar.thumbColor"));
        UIManager.put("ScrollBar.trackColor", UIManager.get("LibraX.workspace"));
        UIManager.put("ScrollBar.buttonColor", UIManager.get("ScrollBar.trackColor"));
        UIManager.put("ScrollBar.hoverArrowColor", colorOf("LibraX.text.secondary"));

        // Focus ring uses the accent so keyboard navigation is visible.
        UIManager.put("Component.focusColor", colorOf("LibraX.accent"));
        UIManager.put("Component.arrowButtonBackground", UIManager.get("LibraX.surface"));
        UIManager.put("Component.arrowButtonBorderColor", colorOf("LibraX.border"));
    }

    private static void color(String flatKey, String tokenKey) {
        UIManager.put(flatKey, colorOf(tokenKey));
    }

    private static Color colorOf(String tokenKey) {
        Color c = UIManager.getColor(tokenKey);
        return c != null ? c : Color.GRAY;
    }

    /** Parses a {@code #RRGGBB} token and stores it in {@link UIManager}. */
    private static void put(String key, String hex) {
        UIManager.put(key, new Color(Integer.parseInt(hex.substring(1), 16)));
    }
}
