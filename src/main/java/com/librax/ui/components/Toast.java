package com.librax.ui.components;

import com.librax.ui.theme.Theme;
import com.librax.ui.theme.UiIcons;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Non-blocking notifications shown in the top-right corner of the window
 * and dismissed automatically after four seconds (DESIGN.md §12).
 *
 * <p>Reserved for successful/acknowledgement-only feedback. Errors and
 * confirmations still use modal dialogs.</p>
 */
public final class Toast {

    /** Semantic kind, which selects the icon and accent color. */
    public enum Kind {
        SUCCESS, INFO, WARNING
    }

    private static final int AUTO_DISMISS_MS = 4000;
    private static final int WRAP_WIDTH = 300;

    private Toast() {
    }

    /**
     * Shows a notification attached to the given component's window.
     *
     * @param parent  any component on the target window; used to find the owner
     * @param message message to display; rendered as plain text
     * @param kind    semantic kind
     */
    public static void show(Component parent, String message, Kind kind) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JWindow window = new JWindow(owner);
        window.setFocusableWindowState(false);

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setBackground(Theme.surface());
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderStrong(), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel icon = new JLabel();
        icon.setIcon(UiIcons.of(iconName(kind), tint(kind), 16));
        content.add(icon, BorderLayout.WEST);

        JLabel text = new JLabel("<html><div style='width:" + WRAP_WIDTH + "px;'>"
                + escape(message) + "</div></html>");
        text.setFont(Theme.body());
        text.setForeground(Theme.textPrimary());
        content.add(text, BorderLayout.CENTER);

        window.setContentPane(content);
        window.pack();
        position(window, owner);
        window.setVisible(true);

        // Click anywhere dismisses early; the timer handles the normal case.
        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                window.dispose();
            }
        });
        Timer timer = new Timer(AUTO_DISMISS_MS, e -> window.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Shows an informational notification.
     *
     * @param parent  component used to locate the owner window
     * @param message message to display
     */
    public static void info(Component parent, String message) {
        show(parent, message, Kind.INFO);
    }

    /**
     * Shows a success notification.
     *
     * @param parent  component used to locate the owner window
     * @param message message to display
     */
    public static void success(Component parent, String message) {
        show(parent, message, Kind.SUCCESS);
    }

    private static void position(JWindow window, Window owner) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x;
        int y;
        if (owner != null && owner.isShowing()) {
            Rectangle b = owner.getBounds();
            x = b.x + b.width - window.getWidth() - 20;
            y = b.y + 56;
        } else {
            x = screen.width - window.getWidth() - 24;
            y = 24;
        }
        window.setLocation(Math.max(0, x), Math.max(0, y));
    }

    private static String iconName(Kind kind) {
        return switch (kind) {
            case SUCCESS -> "check-circle";
            case WARNING -> "alert";
            case INFO -> "info";
        };
    }

    private static Color tint(Kind kind) {
        return switch (kind) {
            case SUCCESS -> Theme.success();
            case WARNING -> Theme.warning();
            case INFO -> Theme.info();
        };
    }

    /** Escapes HTML metacharacters so messages render as plain text. */
    private static String escape(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
