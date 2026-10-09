package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Surface card: white (or dark) fill, 6px radius and a hairline border.
 *
 * <p>Deliberately restrained — no shadows, no gradients — per the design
 * specification. Children are inset by 2px so their square corners never
 * overlap the rounded outline.</p>
 */
public class CardPanel extends JPanel {

    private static final int RADIUS = 6;
    private static final int INSET = 2;

    /**
     * Creates a card with the given layout manager.
     *
     * @param layout layout used to arrange the card contents
     */
    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBackground(Theme.surface());
    }

    /** Card with a standard border layout. */
    public CardPanel() {
        this(new java.awt.BorderLayout());
    }

    @Override
    public Insets getInsets() {
        return new Insets(INSET, INSET, INSET, INSET);
    }

    @Override
    public Insets getInsets(Insets insets) {
        Insets i = getInsets();
        insets.set(i.top, i.left, i.bottom, i.right);
        return insets;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.surface());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        g2.setColor(Theme.border());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, RADIUS, RADIUS));
        g2.dispose();
    }

    /**
     * Convenience factory: card with a titled header and a body region.
     *
     * @param title header text
     * @param body  component placed in the card center
     * @return the configured card
     */
    public static CardPanel titled(String title, JComponent body) {
        CardPanel card = new CardPanel(new java.awt.BorderLayout());
        JPanel header = new JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 14, 9, 14));
        header.add(Ui.sectionTitle(title), java.awt.BorderLayout.WEST);
        card.add(header, java.awt.BorderLayout.NORTH);
        card.add(body, java.awt.BorderLayout.CENTER);
        return card;
    }
}
