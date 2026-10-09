package com.librax.ui.components;

import com.librax.ui.theme.Theme;
import com.librax.ui.theme.UiIcons;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Central factory for the shared control styles (buttons, labels, fields).
 *
 * <p>Every panel builds its controls through this class so spacing, type
 * scale and hover feedback stay identical across the whole application.
 * All values come from {@link Theme}; no component hardcodes a color.</p>
 */
public final class Ui {

    /** Standard control height used by buttons, fields and selects. */
    public static final int CONTROL_HEIGHT = 30;

    private Ui() {
    }

    // ---------------------------------------------------------------- labels

    /** Default body label. */
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.body());
        l.setForeground(Theme.textPrimary());
        return l;
    }

    /** Secondary text for descriptions and hints. */
    public static JLabel caption(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.caption());
        l.setForeground(Theme.textSecondary());
        return l;
    }

    /** Low-emphasis text such as IDs, timestamps and counts. */
    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.caption());
        l.setForeground(Theme.textMuted());
        return l;
    }

    /** Page title used inside dialogs and on the dashboard. */
    public static JLabel pageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.pageTitle());
        l.setForeground(Theme.textPrimary());
        return l;
    }

    /** Section / card title. */
    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.section());
        l.setForeground(Theme.textPrimary());
        return l;
    }

    /** A label with a leading line icon. */
    public static JLabel iconLabel(String iconName, Color tint) {
        JLabel l = new JLabel();
        l.setIcon(UiIcons.of(iconName, tint));
        l.setForeground(tint);
        return l;
    }

    // --------------------------------------------------------------- buttons

    /** Accent-filled primary action. */
    public static JButton primary(String text) {
        return primary(text, null);
    }

    /**
     * Accent-filled primary action, optionally with a leading icon.
     *
     * @param text     button label
     * @param iconName optional icon file name (without extension), may be null
     */
    public static JButton primary(String text, String iconName) {
        JButton b = button(text, iconName);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setForeground(Theme.onAccent());
        hover(b, Theme.accent(), Theme.accentHover());
        return b;
    }

    /** Outlined secondary action — the default for non-destructive verbs. */
    public static JButton secondary(String text) {
        return secondary(text, null);
    }

    /**
     * Outlined secondary action, optionally with a leading icon.
     *
     * @param text     button label
     * @param iconName optional icon file name (without extension), may be null
     */
    public static JButton secondary(String text, String iconName) {
        JButton b = button(text, iconName);
        b.setOpaque(true);
        b.setForeground(Theme.textPrimary());
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderStrong(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        hover(b, Theme.surface(), Theme.surfaceAlt());
        return b;
    }

    /** Destructive action: reserved for delete / irreversible operations. */
    public static JButton danger(String text) {
        JButton b = button(text, null);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setForeground(Color.WHITE);
        hover(b, Theme.danger(), Theme.dangerText());
        return b;
    }

    /** Borderless action used inside toolbars and headers. */
    public static JButton ghost(String text) {
        JButton b = button(text, null);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setForeground(Theme.textSecondary());
        hover(b, Theme.surface(), Theme.neutralSubtle());
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setForeground(Theme.textPrimary());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(Theme.textSecondary());
            }
        });
        return b;
    }

    /**
     * Square icon-only button used in toolbars, rows and headers.
     *
     * @param iconName  icon file name (without extension)
     * @param tooltip   accessible tooltip text
     * @param tint      icon/foreground color
     */
    public static JButton iconButton(String iconName, String tooltip, Color tint) {
        return iconButton(iconName, tooltip, tint, Theme.surface(), Theme.neutralSubtle());
    }

    /**
     * Square icon-only button with explicit background states — used where
     * the surrounding surface differs from the default (e.g. the dark
     * sidebar).
     *
     * @param iconName  icon file name (without extension)
     * @param tooltip   accessible tooltip text
     * @param tint      icon/foreground color
     * @param normalBg  resting background
     * @param hoverBg   background while the pointer is over the button
     */
    public static JButton iconButton(String iconName, String tooltip, Color tint, Color normalBg, Color hoverBg) {
        JButton b = new JButton();
        b.setIcon(UiIcons.of(iconName, tint));
        b.setToolTipText(tooltip);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(true);
        b.setPreferredSize(new Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT));
        b.setMinimumSize(new Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT));
        hover(b, normalBg, hoverBg);
        return b;
    }

    private static JButton button(String text, String iconName) {
        JButton b = new JButton(text);
        b.setFont(Theme.body());
        b.setFocusPainted(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (iconName != null) {
            b.setIcon(UiIcons.of(iconName, b.getForeground()));
            b.setIconTextGap(7);
        }
        b.setBorder(BorderFactory.createEmptyBorder(7, 13, 7, 13));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, CONTROL_HEIGHT));
        return b;
    }

    private static void hover(JButton b, Color base, Color over) {
        b.setBackground(base);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(over);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(base);
            }
        });
        b.addPropertyChangeListener("enabled", evt -> b.setBackground(base));
    }

    // ---------------------------------------------------------------- fields

    /**
     * Styled text input with a placeholder hint.
     *
     * @param placeholder hint text shown while the field is empty
     * @param width       preferred width in pixels
     */
    public static JTextField searchField(String placeholder, int width) {
        JTextField f = new JTextField();
        f.setFont(Theme.body());
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setPreferredSize(new Dimension(width, CONTROL_HEIGHT));
        f.setMinimumSize(new Dimension(140, CONTROL_HEIGHT));
        return f;
    }

    /**
     * Search input with a leading magnifier that focuses the field on click.
     *
     * @param placeholder hint text shown while the field is empty
     * @param width       preferred width in pixels
     * @return container panel ready to drop into a toolbar
     */
    public static JPanel searchGroup(String placeholder, int width) {
        JTextField field = searchField(placeholder, width);
        JLabel icon = iconLabel("search", Theme.textMuted());
        icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                field.requestFocusInWindow();
            }
        });
        JPanel group = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        group.setOpaque(false);
        group.add(icon);
        group.add(field);
        return group;
    }

    /** Convenience accessor for the field inside {@link #searchGroup(String, int)}. */
    public static JTextField fieldOf(JPanel searchGroup) {
        for (Component c : searchGroup.getComponents()) {
            if (c instanceof JTextField f) {
                return f;
            }
        }
        throw new IllegalStateException("searchGroup does not contain a text field");
    }

    /**
     * Styled select control.
     *
     * @param items choices to display
     */
    @SafeVarargs
    public static <T> JComboBox<T> select(T... items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(Theme.body());
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, CONTROL_HEIGHT));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
        return combo;
    }

    // ------------------------------------------------------------ containers

    /** Horizontal cluster of controls with an 8px gap, for toolbars. */
    public static JPanel cluster(Component... children) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (Component c : children) {
            p.add(c);
        }
        return p;
    }

    /** Right-aligned variant of {@link #cluster(Component...)}. */
    public static JPanel clusterRight(Component... children) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        for (Component c : children) {
            p.add(c);
        }
        return p;
    }

    /** Small uppercase group label used in sidebars and settings sections. */
    public static JLabel groupLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.navSection());
        l.setForeground(Theme.textMuted());
        return l;
    }

    /** Standard-size tinted icon, for readability at call sites. */
    public static Icon icon(String name, Color tint) {
        return UiIcons.of(name, tint);
    }
}
