package com.librax.ui.theme;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Read access to the semantic design tokens published by {@link AppTheme},
 * plus the shared typography scale.
 *
 * <p>Every custom component asks this class for colors and fonts instead of
 * constructing {@link Color} or {@link Font} literals, which keeps the whole
 * application consistent and makes theme switching a single operation.</p>
 */
public final class Theme {

    private Theme() {
    }

    // ---------------------------------------------------------------- tokens

    /** Application canvas behind cards and page content. */
    public static Color workspace() { return color("LibraX.workspace"); }

    /** Card, panel, dialog and menu surface. */
    public static Color surface() { return color("LibraX.surface"); }

    /** Alternate table row tint. */
    public static Color surfaceAlt() { return color("LibraX.surfaceAlt"); }

    /** Hairline dividers and card outlines. */
    public static Color border() { return color("LibraX.border"); }

    /** Stronger outlines: dialogs, focused inputs. */
    public static Color borderStrong() { return color("LibraX.borderStrong"); }

    public static Color textPrimary() { return color("LibraX.text.primary"); }

    public static Color textSecondary() { return color("LibraX.text.secondary"); }

    public static Color textMuted() { return color("LibraX.text.muted"); }

    /** Accent used for primary actions, links and the active nav marker. */
    public static Color accent() { return color("LibraX.accent"); }

    public static Color accentHover() { return color("LibraX.accent.hover"); }

    /** Quiet accent fill for selected navigation rows and chips. */
    public static Color accentSubtle() { return color("LibraX.accent.subtle"); }

    /** Accent text that stays readable on {@link #surface()}. */
    public static Color accentText() { return color("LibraX.accent.text"); }

    /** Text/icon color drawn on top of accent-filled buttons. */
    public static Color onAccent() { return color("LibraX.onAccent"); }

    public static Color success() { return color("LibraX.success"); }

    public static Color successSubtle() { return color("LibraX.success.subtle"); }

    public static Color successText() { return color("LibraX.success.text"); }

    public static Color warning() { return color("LibraX.warning"); }

    public static Color warningSubtle() { return color("LibraX.warning.subtle"); }

    public static Color warningText() { return color("LibraX.warning.text"); }

    public static Color danger() { return color("LibraX.danger"); }

    public static Color dangerSubtle() { return color("LibraX.danger.subtle"); }

    public static Color dangerText() { return color("LibraX.danger.text"); }

    public static Color info() { return color("LibraX.info"); }

    public static Color infoSubtle() { return color("LibraX.info.subtle"); }

    public static Color infoText() { return color("LibraX.info.text"); }

    public static Color neutralSubtle() { return color("LibraX.neutral.subtle"); }

    public static Color neutralText() { return color("LibraX.neutral.text"); }

    public static Color sidebarBackground() { return color("LibraX.sidebar.background"); }

    public static Color sidebarBorder() { return color("LibraX.sidebar.border"); }

    public static Color sidebarHover() { return color("LibraX.sidebar.hover"); }

    public static Color sidebarSelected() { return color("LibraX.sidebar.selected"); }

    public static Color sidebarText() { return color("LibraX.sidebar.text"); }

    public static Color sidebarTextSelected() { return color("LibraX.sidebar.text.selected"); }

    public static Color sidebarTextMuted() { return color("LibraX.sidebar.text.muted"); }

    /** Table row selection fill. */
    public static Color selection() { return color("LibraX.selection"); }

    /** Whether the dark palette is currently installed. */
    public static boolean isDark() {
        return UIManager.get(AppTheme.KEY_DARK) instanceof Boolean b && b;
    }

    // ------------------------------------------------------------ typography

    /** Page title used in the top bar (17pt semibold-equivalent). */
    public static Font pageTitle() { return font(Font.BOLD, 17f); }

    /** Section/card titles (13pt bold). */
    public static Font section() { return font(Font.BOLD, 13f); }

    /** Default body text (13pt). */
    public static Font body() { return font(Font.PLAIN, 13f); }

    /** Secondary/help text (11.5pt). */
    public static Font caption() { return font(Font.PLAIN, 11.5f); }

    /** Table cell text (12.5pt) — dense but legible. */
    public static Font table() { return font(Font.PLAIN, 12.5f); }

    /** Table column headers (11.5pt bold). */
    public static Font tableHeader() { return font(Font.BOLD, 11.5f); }

    /** Status badge labels (10.5pt bold). */
    public static Font badge() { return font(Font.BOLD, 10.5f); }

    /** Dashboard KPI numbers (26pt bold). */
    public static Font metric() { return font(Font.BOLD, 26f); }

    /** Section labels in the sidebar (10.5pt bold). */
    public static Font navSection() { return font(Font.BOLD, 10.5f); }

    private static Font font(int style, float size) {
        return new Font(AppTheme.baseFamily(), style, Math.round(size));
    }

    // --------------------------------------------------------------- helpers

    /** Reads a color token, falling back to the FlatLaf default when absent. */
    public static Color color(String key) {
        Color c = UIManager.getColor(key);
        if (c != null) {
            return c;
        }
        Color fallback = UIManager.getColor("Panel.background");
        return fallback != null ? fallback : Color.GRAY;
    }

    /** Returns {@code text} in uppercase, or an empty string when {@code text} is null. */
    public static String normalize(String text) {
        return text == null ? "" : text.toUpperCase();
    }
}
