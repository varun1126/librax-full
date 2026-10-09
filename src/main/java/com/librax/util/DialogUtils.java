package com.librax.util;

import com.librax.ui.components.AppDialog;
import com.librax.ui.components.Toast;
import com.librax.ui.components.Ui;
import com.librax.ui.theme.Theme;
import com.librax.ui.theme.UiIcons;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;

/**
 * Consistent feedback surfaces: modal alerts and confirmations for anything
 * that needs acknowledgement, and non-blocking toasts for successful
 * outcomes (DESIGN.md §7 and §12).
 *
 * <p>Kept as the single entry point so existing call sites stay terse.</p>
 */
public final class DialogUtils {

    private static final int MESSAGE_WRAP = 340;

    private DialogUtils() {
    }

    /**
     * Shows a blocking error alert with an acknowledgement button.
     *
     * @param parent  component used to locate the owner window
     * @param message specific, user-facing failure description
     */
    public static void showError(Component parent, String message) {
        alert(parent, "Something went wrong", message, "alert", Theme.dangerText());
    }

    /**
     * Shows a blocking warning alert with an acknowledgement button.
     *
     * @param parent  component used to locate the owner window
     * @param message specific, user-facing warning
     */
    public static void showWarning(Component parent, String message) {
        alert(parent, "Heads up", message, "alert", Theme.warningText());
    }

    /**
     * Shows a non-blocking success notification.
     *
     * @param parent  component used to locate the owner window
     * @param message what just happened, in plain language
     */
    public static void showInfo(Component parent, String message) {
        Toast.success(parent, message);
    }

    /**
     * Shows a blocking confirmation with explicit copy.
     *
     * @param parent component used to locate the owner window
     * @param message specific description of what will happen
     * @return {@code true} when the user confirms
     */
    public static boolean confirm(Component parent, String message) {
        return confirm(parent, "Please confirm", message, "Confirm", false);
    }

    /**
     * Shows a blocking confirmation with explicit copy and a labelled action.
     *
     * @param parent        component used to locate the owner window
     * @param title         dialog title
     * @param message       specific description of what will happen
     * @param confirmLabel  label of the confirming action
     * @param destructive   whether the confirming action is destructive
     * @return {@code true} when the user confirms
     */
    public static boolean confirm(Component parent, String title, String message,
                                  String confirmLabel, boolean destructive) {
        Window owner = owner(parent);
        ConfirmDialog dialog = new ConfirmDialog(owner, title, message, confirmLabel, destructive);
        dialog.setVisible(true);
        return dialog.confirmed();
    }

    private static void alert(Component parent, String title, String message,
                              String iconName, java.awt.Color tint) {
        AppDialog dialog = new AppDialog(owner(parent), title);
        JPanel body = dialog.body();

        JLabel icon = new JLabel();
        icon.setIcon(UiIcons.of(iconName, tint, 22));
        icon.setPreferredSize(new Dimension(26, 26));
        body.add(icon, "gapright 6, top, wrap");

        JLabel label = new JLabel("<html><div style='width:" + MESSAGE_WRAP + "px;'>"
                + escape(message) + "</div></html>");
        label.setFont(Theme.body());
        label.setForeground(Theme.textPrimary());
        body.add(label, "growx, wrap");

        JButton ok = Ui.primary("OK");
        ok.addActionListener(e -> dialog.dispose());
        dialog.footerRight(ok);
        dialog.setDefaultButton(ok);
        dialog.setVisible(true);
    }

    private static Window owner(Component parent) {
        return parent == null ? null : SwingUtilities.getWindowAncestor(parent);
    }

    private static String escape(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Confirmation dialog exposing the user's decision. */
    private static final class ConfirmDialog extends AppDialog {

        private boolean confirmed;

        private ConfirmDialog(Window owner, String title, String message,
                              String confirmLabel, boolean destructive) {
            super(owner, title);

            JPanel body = body("fillx, ins 16 18 14 18, gap 10");
            JLabel label = new JLabel("<html><div style='width:" + MESSAGE_WRAP + "px;'>"
                    + escape(message) + "</div></html>");
            label.setFont(Theme.body());
            label.setForeground(Theme.textPrimary());
            body.add(label, "growx, wrap");

            JButton cancel = Ui.secondary("Cancel");
            cancel.addActionListener(e -> dispose());

            JButton accept = destructive
                    ? Ui.danger(confirmLabel)
                    : Ui.primary(confirmLabel);
            accept.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            footerRight(cancel);
            footerRight(accept);
            setDefaultButton(accept);
        }

        private boolean confirmed() {
            return confirmed;
        }
    }
}
