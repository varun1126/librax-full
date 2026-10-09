package com.librax.ui.shell;

import com.librax.ui.Section;
import com.librax.ui.components.Ui;
import com.librax.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Compact dark sidebar: brand block, grouped navigation and the signed-in
 * user card with a sign-out control.
 *
 * <p>Navigation is grouped (Overview / Catalog / Circulation / System) so
 * the eight sections stay scannable without scrolling.</p>
 */
public class SidebarPanel extends JPanel {

    private static final int WIDTH = 216;

    private final Map<Section, NavButton> buttons = new EnumMap<>(Section.class);

    /**
     * Builds the sidebar.
     *
     * @param onSelect  callback invoked when a section is chosen
     * @param onSignOut callback invoked by the sign-out control
     * @param userName  display name of the signed-in user
     * @param role      role label shown under the name
     */
    public SidebarPanel(Consumer<Section> onSelect, Runnable onSignOut, String userName, String role) {
        super(new BorderLayout());
        this.onSelect = onSelect;
        setBackground(Theme.sidebarBackground());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.sidebarBorder()));
        setPreferredSize(new Dimension(WIDTH, 0));
        setMinimumSize(new Dimension(WIDTH, 0));

        add(buildBrand(), BorderLayout.NORTH);
        add(buildNav(), BorderLayout.CENTER);
        add(buildUserCard(userName, role, onSignOut), BorderLayout.SOUTH);
    }

    private final Consumer<Section> onSelect;

    /**
     * Marks a section as the active destination.
     *
     * @param section section to highlight; {@code null} clears the highlight
     */
    public void setActive(Section section) {
        buttons.forEach((key, button) -> button.setActive(key == section));
    }

    // ------------------------------------------------------------------ brand

    private JPanel buildBrand() {
        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        brand.add(new Monogram(), BorderLayout.WEST);

        JPanel titles = new JPanel(new BorderLayout(0, 1));
        titles.setOpaque(false);
        JLabel name = new JLabel("LibraX");
        name.setFont(Theme.body().deriveFont(Font.BOLD, 13.5f));
        name.setForeground(Theme.sidebarTextSelected());
        JLabel subtitle = new JLabel("Library Management");
        subtitle.setFont(Theme.caption().deriveFont(11f));
        subtitle.setForeground(Theme.sidebarTextMuted());
        titles.add(name, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);
        brand.add(titles, BorderLayout.CENTER);
        return brand;
    }

    // -------------------------------------------------------------------- nav

    private JScrollPane buildNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new javax.swing.BoxLayout(nav, javax.swing.BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));

        for (String group : Section.GROUPS) {
            JLabel groupLabel = new JLabel(group);
            groupLabel.setFont(Theme.navSection());
            groupLabel.setForeground(Theme.sidebarTextMuted());
            groupLabel.setAlignmentX(LEFT_ALIGNMENT);
            groupLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 0));
            nav.add(groupLabel);

            for (Section section : Section.inGroup(group)) {
                NavButton button = new NavButton(section, onSelect);
                buttons.put(section, button);
                nav.add(button);
            }
        }

        JScrollPane scroll = new JScrollPane(nav);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(Theme.sidebarBackground());
        scroll.getViewport().setBackground(Theme.sidebarBackground());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ------------------------------------------------------------------- user

    private JPanel buildUserCard(String userName, String role, Runnable onSignOut) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setOpaque(true);
        card.setBackground(Theme.sidebarBackground());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.sidebarBorder()),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        card.add(new Avatar(userName), BorderLayout.WEST);

        JPanel info = new JPanel(new BorderLayout(0, 1));
        info.setOpaque(false);
        String display = userName == null || userName.isBlank() ? "Signed out" : userName;
        JLabel nameLabel = new JLabel(truncate(display, 22));
        nameLabel.setFont(Theme.caption().deriveFont(Font.BOLD, 12f));
        nameLabel.setForeground(Theme.sidebarTextSelected());
        JLabel roleLabel = new JLabel(role == null ? "" : role.toUpperCase());
        roleLabel.setFont(Theme.navSection());
        roleLabel.setForeground(Theme.sidebarTextMuted());
        info.add(nameLabel, BorderLayout.NORTH);
        info.add(roleLabel, BorderLayout.SOUTH);
        card.add(info, BorderLayout.CENTER);

        JButton signOut = Ui.iconButton("logout", "Sign out", Theme.sidebarText(),
                Theme.sidebarBackground(), Theme.sidebarHover());
        signOut.addActionListener(e -> onSignOut.run());
        card.add(signOut, BorderLayout.EAST);
        return card;
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    // ------------------------------------------------------------------ marks

    /** App monogram: accent tile with the LX initials. */
    private static final class Monogram extends JComponent {
        private static final int SIZE = 26;

        private Monogram() {
            setPreferredSize(new Dimension(SIZE, SIZE));
            setMaximumSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.accent());
            g2.fillRoundRect(0, 0, SIZE, SIZE, 6, 6);
            g2.setFont(getFont().deriveFont(Font.BOLD, 11.5f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            String text = "LX";
            g2.drawString(text, (SIZE - fm.stringWidth(text)) / 2,
                    (SIZE - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Circular avatar with the user's initials. */
    private static final class Avatar extends JComponent {
        private static final int SIZE = 28;

        private final String initials;

        private Avatar(String userName) {
            this.initials = initialsOf(userName);
            setPreferredSize(new Dimension(SIZE, SIZE));
            setMaximumSize(new Dimension(SIZE, SIZE));
            setToolTipText(userName);
        }

        private static String initialsOf(String name) {
            if (name == null || name.isBlank()) {
                return "?";
            }
            String[] parts = name.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            sb.append(Character.toUpperCase(parts[0].charAt(0)));
            if (parts.length > 1 && !parts[parts.length - 1].isBlank()) {
                sb.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
            }
            return sb.toString();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.accent());
            g2.fillOval(0, 0, SIZE, SIZE);
            g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(initials, (SIZE - fm.stringWidth(initials)) / 2,
                    (SIZE - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
