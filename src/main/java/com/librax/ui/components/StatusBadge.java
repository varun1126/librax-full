package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Compact status label: a tinted, slightly rounded chip carrying a short
 * uppercase state word (AVAILABLE, OVERDUE, READY…).
 *
 * <p>Built to work both as a standalone component (dashboard lists, forms)
 * and as a table cell renderer via {@link BadgeCellRenderer}.</p>
 */
public class StatusBadge extends JComponent {

    private static final int RADIUS = 4;
    private static final int PAD = 8;
    private static final int CHIP_HEIGHT = 19;

    private String text = "";
    private BadgeTone tone = BadgeTone.NEUTRAL;

    public StatusBadge() {
        setOpaque(false);
        setFont(Theme.badge());
    }

    /**
     * Creates a badge with an initial state.
     *
     * @param text state text; rendered uppercased, {@code null} is tolerated
     * @param tone semantic tone controlling the colors
     */
    public StatusBadge(String text, BadgeTone tone) {
        this();
        setStatus(text, tone);
    }

    /**
     * Updates the displayed state.
     *
     * @param text state text; rendered uppercased, {@code null} is tolerated
     * @param tone semantic tone controlling the colors
     */
    public void setStatus(String text, BadgeTone tone) {
        this.text = text == null ? "" : text.trim().toUpperCase();
        this.tone = tone == null ? BadgeTone.NEUTRAL : tone;
        revalidate();
        repaint();
    }

    /** The uppercased text currently displayed. */
    public String getBadgeText() {
        return text;
    }

    /** The tone currently applied. */
    public BadgeTone getTone() {
        return tone;
    }

    @Override
    public Dimension getPreferredSize() {
        if (text.isEmpty()) {
            return new Dimension(PAD * 2, CHIP_HEIGHT);
        }
        FontMetrics fm = getFontMetrics(Theme.badge());
        return new Dimension(fm.stringWidth(text) + PAD * 2, CHIP_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintChip((Graphics2D) g, 0, getWidth(), getHeight());
    }

    /**
     * Paints the chip at the given left offset inside an area.
     *
     * @param g       graphics context
     * @param left    x offset where the chip starts
     * @param areaW   width of the surrounding area (used for cell rendering)
     * @param areaH   height of the surrounding area (the chip is centered)
     */
    protected final void paintChip(Graphics g, int left, int areaW, int areaH) {
        if (text.isEmpty()) {
            return;
        }
        g.setFont(Theme.badge());
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text) + PAD * 2;
        int h = Math.min(CHIP_HEIGHT, areaH);
        int y = Math.max(0, (areaH - h) / 2);

        ((Graphics2D) g).setColor(tone.background());
        g.fillRoundRect(left, y, w, h, RADIUS, RADIUS);

        g.setColor(tone.foreground());
        g.drawString(text, left + PAD, y + (h - fm.getHeight()) / 2 + fm.getAscent());
    }
}
