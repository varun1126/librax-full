package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;

/**
 * Shared scaffold for every list-oriented screen (books, members, loans,
 * reservations, fines): toolbar row, bordered table card, status footer and
 * a designed empty state.
 *
 * <p>Subclasses supply columns via {@code super(columns)} and load data in
 * {@link #reload()}. Rows are pushed with {@link #setRows}.</p>
 */
public abstract class TablePage extends JPanel {

    protected final DefaultTableModel model;
    protected final JTable table;

    private final JPanel leftTools;
    private final JPanel rightTools;
    private final JLabel statusLabel = Ui.muted("0 records");
    private final JPanel body = new JPanel(new CardLayout());
    private final JScrollPane scroll;
    private final EmptyState emptyState = new EmptyState();

    private String nounPlural = "records";

    /**
     * Builds the page scaffold.
     *
     * @param columns table column headers in display order
     */
    protected TablePage(String... columns) {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 18, 20));
        setBackground(Theme.workspace());

        leftTools = Ui.cluster();
        rightTools = Ui.clusterRight();
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        model = Tables.model(columns);
        table = Tables.table(model);
        table.setAutoCreateRowSorter(false);

        scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.surface());
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        body.setOpaque(false);
        body.add(scroll, "table");
        body.add(emptyState, "empty");

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.surface());
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        footer.add(statusLabel, BorderLayout.WEST);

        CardPanel card = new CardPanel(new BorderLayout());
        card.add(body, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }

    // ----------------------------------------------------------- toolbar API

    /** Left toolbar cluster: search field and filters. */
    protected final JPanel leftTools() {
        return leftTools;
    }

    /** Right toolbar cluster: row and primary actions. */
    protected final JPanel rightTools() {
        return rightTools;
    }

    /** The configured table. */
    protected final JTable table() {
        return table;
    }

    /** The underlying read-only model. */
    protected final DefaultTableModel tableModel() {
        return model;
    }

    /** Applies a search field wired to {@link #refresh()} on Enter. */
    protected final JTextField addSearch(String placeholder, int width) {
        JPanel group = Ui.searchGroup(placeholder, width);
        JTextField field = Ui.fieldOf(group);
        field.addActionListener(e -> refresh());
        leftTools().add(group);
        return field;
    }

    /** Configures what is shown when the table has no rows. */
    protected final void configureEmpty(String iconName, String message, String hint) {
        emptyState.setState(iconName, message, hint);
    }

    /** Adds an action button to the empty state. */
    protected final void configureEmptyAction(String label, Runnable action) {
        emptyState.setAction(label, action);
    }

    // ----------------------------------------------------------- data plumbing

    /**
     * Replaces the visible rows and updates the record count.
     *
     * @param rows       rows in model-column order ({@code null} cells render blank)
     * @param nounPlural plural noun for the footer count, e.g. {@code "books"}
     */
    protected final void setRows(java.util.List<Object[]> rows, String nounPlural) {
        this.nounPlural = nounPlural;
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
        ((CardLayout) body.getLayout()).show(body, rows.isEmpty() ? "empty" : "table");
        statusLabel.setText(countLabel(rows.size()));
    }

    private String countLabel(int count) {
        String noun = nounPlural;
        if (count == 1 && noun.length() > 1 && noun.endsWith("s")) {
            noun = noun.substring(0, noun.length() - 1);
        }
        return count + " " + noun;
    }

    /** Re-reads data and repaints the page. */
    public final void refresh() {
        reload();
    }

    /**
     * Loads data from the service layer and pushes it through
     * {@link #setRows(java.util.List, String)}.
     */
    protected abstract void reload();

    /** Exposes the toolbar so panels can add trailing components directly. */
    protected final void addToolbarLeft(Component component) {
        leftTools.add(component);
    }

    /** Exposes the toolbar so panels can add trailing components directly. */
    protected final void addToolbarRight(Component component) {
        rightTools.add(component);
    }

    /** Returns the empty-state component for panels that customize it further. */
    protected final EmptyState emptyState() {
        return emptyState;
    }

    /** Returns the status footer label (record count). */
    protected final JLabel statusLabel() {
        return statusLabel;
    }

    /** Builds a titled card body for secondary content areas inside a page. */
    protected static CardPanel card(String title, JComponent body) {
        return CardPanel.titled(title, body);
    }

    /** Buttons commonly used in the toolbar; exposed for reuse in subclasses. */
    protected static JButton toolbarButton(String text, String iconName) {
        return Ui.secondary(text, iconName);
    }
}
