package com.librax.ui.shell;

import com.librax.ui.Section;
import com.librax.ui.components.Ui;
import com.librax.ui.theme.Theme;
import com.librax.ui.theme.UiIcons;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.Consumer;

/**
 * Workspace top bar: current section title and subtitle on the left,
 * global book search and the theme toggle on the right.
 */
public class TopBar extends JPanel {

    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JTextField searchField;
    private final JButton themeButton;

    /**
     * Builds the bar.
     *
     * @param onSearch      invoked with the query when the user presses Enter
     * @param onToggleTheme invoked when the theme toggle is pressed
     */
    public TopBar(Consumer<String> onSearch, Runnable onToggleTheme) {
        super(new BorderLayout(16, 0));
        setBackground(Theme.surface());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                BorderFactory.createEmptyBorder(8, 20, 8, 16)));
        setPreferredSize(new Dimension(120, 56));

        titleLabel.setFont(Theme.pageTitle());
        titleLabel.setForeground(Theme.textPrimary());
        subtitleLabel.setFont(Theme.caption());
        subtitleLabel.setForeground(Theme.textSecondary());

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(titleLabel);
        titles.add(subtitleLabel);
        add(titles, BorderLayout.WEST);

        searchField = Ui.searchField("Search books by title, author or ISBN", 250);
        searchField.addActionListener(e -> onSearch.accept(searchField.getText()));

        themeButton = Ui.iconButton("moon", "Switch to dark theme", Theme.textSecondary());
        themeButton.addActionListener(e -> onToggleTheme.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(searchField);
        right.add(themeButton);
        add(right, BorderLayout.EAST);

        setSection(Section.DASHBOARD);
    }

    /**
     * Updates the title block for the current section.
     *
     * @param section section being displayed
     */
    public void setSection(Section section) {
        titleLabel.setText(section.title);
        subtitleLabel.setText(section.subtitle);
    }

    /** The global search field (books). */
    public JTextField searchField() {
        return searchField;
    }

    /**
     * Refreshes the theme toggle icon for the installed palette.
     *
     * @param dark whether the dark palette is active
     */
    public void applyThemeState(boolean dark) {
        themeButton.setIcon(UiIcons.of(dark ? "sun" : "moon", Theme.textSecondary()));
        themeButton.setToolTipText(dark ? "Switch to light theme" : "Switch to dark theme");
    }
}
