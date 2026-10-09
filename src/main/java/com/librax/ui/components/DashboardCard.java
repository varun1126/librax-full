package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Dashboard KPI tile: quiet label, prominent figure and an optional
 * context line (for example “across 42 titles”).
 */
public class DashboardCard extends CardPanel {

    private final JLabel valueLabel;
    private final JLabel footLabel;

    /**
     * Creates a KPI tile.
     *
     * @param title label describing the metric
     * @param value initial figure
     */
    public DashboardCard(String title, String value) {
        super(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.caption());
        titleLabel.setForeground(Theme.textSecondary());

        valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.metric());
        valueLabel.setForeground(Theme.textPrimary());

        footLabel = new JLabel(" ");
        footLabel.setFont(Theme.caption());
        footLabel.setForeground(Theme.textMuted());

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(footLabel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(180, 104));
    }

    /**
     * Updates the figure.
     *
     * @param value new figure text
     */
    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /**
     * Updates the context line under the figure.
     *
     * @param text context copy; blank hides the line
     */
    public void setFoot(String text) {
        footLabel.setText(text == null || text.isBlank() ? " " : text);
    }
}
