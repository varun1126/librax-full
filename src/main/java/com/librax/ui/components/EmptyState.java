package com.librax.ui.components;

import com.librax.ui.theme.Theme;
import com.librax.ui.theme.UiIcons;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Designed empty state: icon, one-line explanation, optional hint and an
 * optional recovery action (DESIGN.md §13).
 */
public class EmptyState extends JPanel {

    private final JLabel iconLabel = new JLabel();
    private final JLabel messageLabel = new JLabel("", JLabel.CENTER);
    private final JLabel hintLabel = new JLabel("", JLabel.CENTER);
    private JButton actionButton;

    public EmptyState() {
        super(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(36, 24, 36, 24));

        iconLabel.setIcon(UiIcons.of("file-text", Theme.textMuted(), UiIcons.LARGE));

        messageLabel.setFont(Theme.body());
        messageLabel.setForeground(Theme.textSecondary());

        hintLabel.setFont(Theme.caption());
        hintLabel.setForeground(Theme.textMuted());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 10, 0);
        add(iconLabel, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 4, 0);
        add(messageLabel, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 0, 0);
        add(hintLabel, gc);
    }

    /**
     * Configures the state copy.
     *
     * @param iconName icon file name (without extension)
     * @param message  one-line explanation shown as the headline
     * @param hint     optional secondary line; {@code null} or blank hides it
     */
    public void setState(String iconName, String message, String hint) {
        Color tint = Theme.textMuted();
        iconLabel.setIcon(iconName == null ? UiIcons.of("file-text", tint, UiIcons.LARGE)
                : UiIcons.of(iconName, tint, UiIcons.LARGE));
        messageLabel.setText(message == null ? "" : message);
        boolean hasHint = hint != null && !hint.isBlank();
        hintLabel.setText(hasHint ? hint : " ");
        hintLabel.setVisible(hasHint);
    }

    /**
     * Adds a recovery action below the copy.
     *
     * @param label  button label
     * @param action invoked when the button is pressed; {@code null} removes it
     */
    public void setAction(String label, Runnable action) {
        if (actionButton != null) {
            remove(actionButton);
            actionButton = null;
        }
        if (label != null && action != null) {
            actionButton = Ui.secondary(label, "plus");
            actionButton.addActionListener(e -> action.run());
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 4;
            gc.insets = new Insets(14, 0, 0, 0);
            add(actionButton, gc);
        }
        revalidate();
        repaint();
    }
}
