package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Base dialog: in-application title bar (draggable, with a close control),
 * padded body area and a right-aligned button footer.
 *
 * <p>All dialogs — confirmations, alerts and forms — are built on this class
 * so chrome, spacing and button placement stay identical everywhere
 * (DESIGN.md §7).</p>
 */
public class AppDialog extends JDialog {

    private final JPanel bodyHolder = new JPanel(new BorderLayout());
    private final JPanel leftCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JPanel rightCluster = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    /**
     * Creates a modal, undecorated dialog.
     *
     * @param owner owner window used for centering; may be {@code null}
     * @param title title shown in the dialog header
     */
    public AppDialog(Window owner, String title) {
        super(owner, null, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        JPanel header = buildHeader(title);
        bodyHolder.setOpaque(true);
        bodyHolder.setBackground(Theme.surface());

        leftCluster.setOpaque(false);
        rightCluster.setOpaque(false);
        JPanel footer = new JPanel(new net.miginfocom.swing.MigLayout("fillx, ins 0, gap 6"));
        footer.setBackground(Theme.surface());
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 12, 16));
        footer.add(leftCluster, "growx");
        footer.add(rightCluster, "right");

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.surface());
        root.setBorder(BorderFactory.createLineBorder(Theme.borderStrong(), 1));
        root.add(header, BorderLayout.NORTH);
        root.add(bodyHolder, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);

        installKeyBindings();
    }

    private JPanel buildHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.surface());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));
        header.setPreferredSize(new Dimension(120, 42));

        JLabel titleLabel = new JLabel(title == null ? "" : title);
        titleLabel.setFont(Theme.section());
        titleLabel.setForeground(Theme.textPrimary());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        header.add(titleLabel, BorderLayout.WEST);

        JButton close = Ui.iconButton("close", "Close (Esc)", Theme.textSecondary());
        close.addActionListener(e -> dispose());
        header.add(close, BorderLayout.EAST);

        // Dragging by the header (title label included) moves the dialog.
        MouseAdapter drag = new MouseAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent e) {
                offset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (offset == null) {
                    return;
                }
                Point p = getLocation();
                setLocation(p.x + e.getX() - offset.x, p.y + e.getY() - offset.y);
            }
        };
        header.addMouseListener(drag);
        header.addMouseMotionListener(drag);
        titleLabel.addMouseListener(drag);
        titleLabel.addMouseMotionListener(drag);
        return header;
    }

    private void installKeyBindings() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "librax.dialog.close");
        getRootPane().getActionMap().put("librax.dialog.close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * Adds a body section using the given MigLayout constraints.
     *
     * @param constraints MigLayout layout constraints for the body panel
     * @return the body panel to populate
     */
    public JPanel body(String constraints) {
        JPanel panel = new JPanel(new net.miginfocom.swing.MigLayout(constraints));
        panel.setBackground(Theme.surface());
        bodyHolder.add(panel, BorderLayout.CENTER);
        return panel;
    }

    /** Body panel with the standard form padding and rhythm. */
    public JPanel body() {
        return body("fillx, ins 16 18 16 18, gap 10");
    }

    /** Adds a de-emphasized action at the footer's left edge. */
    public void footerLeft(java.awt.Component button) {
        leftCluster.add(button);
    }

    /**
     * Adds an action to the footer's right cluster; add the primary action
     * last so it sits rightmost.
     *
     * @param button button to add
     */
    public void footerRight(java.awt.Component button) {
        rightCluster.add(button);
    }

    /** Makes the button the default so Enter activates it. */
    public void setDefaultButton(JButton button) {
        getRootPane().setDefaultButton(button);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            pack();
            if (getWidth() < 400) {
                setSize(400, getHeight());
            }
            setMinimumSize(getSize());
            setLocationRelativeTo(getOwner());
        }
        super.setVisible(visible);
    }
}
