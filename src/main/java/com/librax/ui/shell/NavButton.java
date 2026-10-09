package com.librax.ui.shell;

import com.librax.ui.Section;
import com.librax.ui.theme.Theme;
import com.librax.ui.theme.UiIcons;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Sidebar navigation item: flat row with a line icon, label, hover state
 * and a left accent marker while active. Painting is done manually so the
 * dark sidebar keeps complete control of its states.
 */
public class NavButton extends JButton {

    private static final int BAR_WIDTH = 3;
    private static final int ICON_X = 16;
    private static final int HEIGHT = 34;

    private final Section section;
    private boolean hover;
    private boolean active;

    /**
     * Creates a navigation row.
     *
     * @param section  section this row navigates to
     * @param onSelect callback invoked on click
     */
    public NavButton(Section section, Consumer<Section> onSelect) {
        this.section = section;
        setText(section.title);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setFont(Theme.body());
        setForeground(Theme.sidebarText());
        setIcon(UiIcons.of(section.iconName, Theme.sidebarText(), UiIcons.NAV));
        setIconTextGap(10);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(section.title);
        setAlignmentX(LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(200, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        setMinimumSize(new Dimension(120, HEIGHT));

        addActionListener(e -> onSelect.accept(section));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    /** The section this row represents. */
    public Section section() {
        return section;
    }

    /**
     * Marks the row as the current section.
     *
     * @param selected whether this row is active
     */
    public void setActive(boolean selected) {
        if (this.active == selected) {
            return;
        }
        this.active = selected;
        Color tint = selected ? Theme.sidebarTextSelected() : Theme.sidebarText();
        setIcon(UiIcons.of(section.iconName, tint, UiIcons.NAV));
        repaint();
    }

    /** @return whether this row is the active section */
    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        if (active) {
            g2.setColor(Theme.sidebarSelected());
            g2.fillRect(0, 0, w, h);
            g2.setColor(Theme.accent());
            g2.fillRect(0, 0, BAR_WIDTH, h);
        } else if (hover) {
            g2.setColor(Theme.sidebarHover());
            g2.fillRect(0, 0, w, h);
        }

        Color text = active || hover ? Theme.sidebarTextSelected() : Theme.sidebarText();
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = ICON_X;

        Icon icon = getIcon();
        if (icon != null) {
            icon.paintIcon(this, g2, x, (h - icon.getIconHeight()) / 2);
            x += icon.getIconWidth() + getIconTextGap();
        }

        int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(text);
        g2.drawString(getText(), x, baseline);

        if (isFocusOwner()) {
            g2.setColor(Theme.accent());
            g2.drawRect(1, 1, w - 3, h - 3);
        }
        g2.dispose();
    }
}
