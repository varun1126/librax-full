package com.librax.ui.components;

import com.librax.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Dimension;

/**
 * Builds dialog forms with the prescribed structure: label above field,
 * consistent 30px control height, required-field asterisk and consistent
 * vertical rhythm (DESIGN.md §8).
 */
public class FormBuilder {

    private final JPanel panel;
    private int rows;

    /** Creates an empty form builder with its own panel. */
    public FormBuilder() {
        this(new JPanel(new MigLayout("fillx, ins 0, gap 0 0", "[grow]")));
    }

    /**
     * Creates a builder that fills an existing panel.
     *
     * @param panel panel using a single-column MigLayout grid
     */
    public FormBuilder(JPanel panel) {
        this.panel = panel;
    }

    /** The panel holding every field added so far. */
    public JPanel panel() {
        return panel;
    }

    /**
     * Adds a single-line text field.
     *
     * @param label     field label
     * @param value     initial value, may be {@code null}
     * @param required  whether the field must be filled in
     * @return the editable field
     */
    public JTextField textField(String label, String value, boolean required) {
        JTextField field = new JTextField(value == null ? "" : value);
        add(label, field, required);
        return field;
    }

    /**
     * Adds a password field.
     *
     * @param label    field label
     * @param required whether the field must be filled in
     * @return the editable field
     */
    public JPasswordField passwordField(String label, boolean required) {
        JPasswordField field = new JPasswordField();
        add(label, field, required);
        return field;
    }

    /**
     * Adds a select control.
     *
     * @param <T>      choice type
     * @param label    field label
     * @param items    choices
     * @param required whether a choice must be made
     * @return the select control
     */
    public final <T> JComboBox<T> comboBox(String label, T[] items, boolean required) {
        JComboBox<T> combo = Ui.select(items);
        add(label, combo, required);
        return combo;
    }

    /**
     * Adds a generic control with a label above it.
     *
     * @param label    field label
     * @param field    control to place below the label
     * @param required whether an asterisk should mark the field
     */
    public void add(String label, JComponent field, boolean required) {
        JLabel lab = new JLabel(label + (required ? "  *" : ""));
        lab.setFont(Theme.caption());
        lab.setForeground(Theme.textSecondary());
        panel.add(lab, "growx, wrap, gaptop " + (rows == 0 ? 0 : 12));

        field.setPreferredSize(new Dimension(field.getPreferredSize().width, Ui.CONTROL_HEIGHT));
        panel.add(field, "growx, wrap, gaptop 4");
        rows++;
    }

    /**
     * Adds a full-width informational line (help text or validation hint).
     *
     * @param text caption copy
     */
    public void hint(String text) {
        JLabel lab = new JLabel(text);
        lab.setFont(Theme.caption());
        lab.setForeground(Theme.textMuted());
        panel.add(lab, "growx, wrap, gaptop 6");
    }

    /** Adds vertical space between form groups. */
    public void gap(int pixels) {
        panel.add(new javax.swing.Box.Filler(new Dimension(0, 0), new Dimension(0, pixels),
                new Dimension(0, pixels)), "growx, wrap");
    }
}
