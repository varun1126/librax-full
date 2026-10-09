package com.librax.ui.components;

import com.librax.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Comparator;
import java.util.function.Function;

/**
 * Table conventions shared by every list view: dense rows, zebra striping,
 * quiet headers, null-safe sorting, right-aligned numerics and status chips.
 */
public final class Tables {

    /** Sorts any column type safely: nulls last, numbers/dates numerically. */
    private static final Comparator<Object> SAFE = (a, b) -> {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a instanceof String sa && b instanceof String sb) {
            return sa.compareToIgnoreCase(sb);
        }
        if (a instanceof Comparable<?> ca && a.getClass() == b.getClass()) {
            @SuppressWarnings("unchecked")
            Comparable<Object> natural = (Comparable<Object>) ca;
            return natural.compareTo(b);
        }
        return String.valueOf(a).compareToIgnoreCase(String.valueOf(b));
    };

    private Tables() {
    }

    // ------------------------------------------------------------- factories

    /**
     * Creates a read-only table model with the given columns.
     *
     * @param columns column headers in display order
     * @return a model whose cells are never editable
     */
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * Creates a table pre-styled according to the design system.
     *
     * @param columns column headers in display order
     * @return table ready for rows
     */
    public static JTable table(String... columns) {
        return table(model(columns));
    }

    /**
     * Styles an existing read-only model's table.
     *
     * @param tableModel model supplying the columns
     * @return table ready for rows
     */
    public static JTable table(DefaultTableModel tableModel) {
        JTable table = new JTable(tableModel);
        style(table);
        return table;
    }

    /**
     * Applies the shared table style to an existing table.
     *
     * @param table table to style in place
     */
    public static void style(JTable table) {
        table.setFont(Theme.table());
        table.setForeground(Theme.textPrimary());
        table.setBackground(Theme.surface());
        table.setOpaque(true);
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(Theme.selection());
        table.setSelectionForeground(Theme.textPrimary());
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.setDefaultRenderer(String.class, new CellRenderer());
        table.setDefaultRenderer(Integer.class, new CellRenderer());
        table.setDefaultRenderer(Long.class, new CellRenderer());

        if (table.getModel() instanceof DefaultTableModel dm) {
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(dm);
            for (int i = 0; i < dm.getColumnCount(); i++) {
                sorter.setComparator(i, SAFE);
            }
            table.setRowSorter(sorter);
        }

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 30));
        header.setDefaultRenderer(new HeaderRenderer(SwingConstants.LEFT));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.borderStrong()));
    }

    // ------------------------------------------------------------ refinements

    /**
     * Renders a column as a status chip.
     *
     * @param table         target table
     * @param modelColumn   column index in the model
     */
    public static void statusColumn(JTable table, int modelColumn) {
        statusColumn(table, modelColumn, BadgeTone::of);
    }

    /**
     * Renders a column as a status chip with a caller-supplied tone mapping.
     *
     * @param table       target table
     * @param modelColumn column index in the model
     * @param tone        resolves each raw value to a {@link BadgeTone}
     */
    public static void statusColumn(JTable table, int modelColumn, Function<Object, BadgeTone> tone) {
        column(table, modelColumn).setCellRenderer(new BadgeCellRenderer(tone));
    }

    /**
     * Right-aligns a numeric column and its header.
     *
     * @param table       target table
     * @param modelColumn column index in the model
     */
    public static void rightAlign(JTable table, int modelColumn) {
        column(table, modelColumn).setCellRenderer(new CellRenderer(SwingConstants.RIGHT, Theme.table(), false));
        column(table, modelColumn).setHeaderRenderer(new HeaderRenderer(SwingConstants.RIGHT));
    }

    /**
     * Styles an identifier column: narrow and de-emphasized.
     *
     * @param table       target table
     * @param modelColumn column index in the model
     */
    public static void idColumn(JTable table, int modelColumn) {
        TableColumn c = column(table, modelColumn);
        c.setMinWidth(48);
        c.setMaxWidth(72);
        c.setPreferredWidth(56);
        c.setCellRenderer(new CellRenderer(SwingConstants.LEFT, Theme.caption(), true));
    }

    /**
     * Sets preferred widths for columns in order; {@code -1} keeps the default.
     *
     * @param table   target table
     * @param widths  preferred widths per column index
     */
    public static void widths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            if (widths[i] > 0) {
                column(table, i).setPreferredWidth(widths[i]);
            }
        }
    }

    /**
     * Removes the column header (used for compact list tables).
     *
     * @param table target table
     */
    public static void noHeader(JTable table) {
        table.setTableHeader(null);
    }

    private static TableColumn column(JTable table, int modelColumn) {
        return table.getColumnModel().getColumn(modelColumn);
    }

    // -------------------------------------------------------------- renderers

    /** Default cell renderer: padding, theme colors and zebra striping. */
    static class CellRenderer extends DefaultTableCellRenderer {

        private final int align;
        private final Font font;
        private final boolean muted;

        CellRenderer() {
            this(SwingConstants.LEFT, Theme.table(), false);
        }

        CellRenderer(int align, Font font, boolean muted) {
            this.align = align;
            this.font = font;
            this.muted = muted;
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value == null ? "" : String.valueOf(value));
            setHorizontalAlignment(align);
            setFont(font);
            setBackground(isSelected ? Theme.selection()
                    : (row % 2 == 1 ? Theme.surfaceAlt() : Theme.surface()));
            setForeground(isSelected ? Theme.textPrimary()
                    : (muted ? Theme.textMuted() : Theme.textPrimary()));
            return this;
        }
    }

    /** Header renderer with consistent type, padding and alignment. */
    static class HeaderRenderer extends DefaultTableCellRenderer {

        private final int align;

        HeaderRenderer(int align) {
            this.align = align;
            setOpaque(true);
            setFont(Theme.tableHeader());
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value == null ? "" : String.valueOf(value));
            setHorizontalAlignment(align);
            setFont(Theme.tableHeader());
            setForeground(Theme.textSecondary());
            setBackground(Theme.surface());
            return this;
        }
    }

    /**
     * Renders status chips inside table cells, including the cell background
     * (selection state and zebra striping) behind the chip.
     */
    static class BadgeCellRenderer extends StatusBadge implements TableCellRenderer {

        private final Function<Object, BadgeTone> tone;
        private boolean selected;
        private boolean striped;

        BadgeCellRenderer(Function<Object, BadgeTone> tone) {
            this.tone = tone;
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setStatus(value == null ? "" : String.valueOf(value), tone.apply(value));
            selected = isSelected;
            striped = row % 2 == 1;
            setFont(Theme.badge());
            return this;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            g.setColor(selected ? Theme.selection()
                    : (striped ? Theme.surfaceAlt() : Theme.surface()));
            g.fillRect(0, 0, getWidth(), getHeight());
            paintChip(g, 10, getWidth(), getHeight());
        }
    }
}
