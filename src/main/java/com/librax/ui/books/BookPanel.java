package com.librax.ui.books;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Book;
import com.librax.ui.components.BadgeTone;
import com.librax.ui.components.TablePage;
import com.librax.ui.components.Tables;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalog screen: searchable, filterable book list with the row actions
 * (edit / delete), bulk import from Google Books and add-book primary
 * action.
 */
public class BookPanel extends TablePage {

    private static final String ALL_CATEGORIES = "All categories";

    private final AppContext ctx;
    private final JTextField search;
    private final JComboBox<String> categoryFilter;
    private final JButton editButton = Ui.secondary("Edit", "edit");
    private final JButton deleteButton = Ui.secondary("Delete", "trash");

    /**
     * Builds the page and performs the first load.
     *
     * @param ctx composition root providing the book service
     */
    public BookPanel(AppContext ctx) {
        super("ID", "Title", "Author", "Category", "ISBN", "Copies", "Status");
        this.ctx = ctx;

        configureEmpty("book", "No books match your search",
                "Adjust the filters, or add a title to the catalog.");
        configureEmptyAction("Add book", this::addBook);

        search = addSearch("Search title, author or ISBN…", 240);

        categoryFilter = Ui.select(categories());
        categoryFilter.addActionListener(e -> refresh());
        leftTools().add(categoryFilter);

        JButton importButton = Ui.secondary("Import from Google Books", "download");
        importButton.addActionListener(e -> new GoogleBooksSearchDialog(
                SwingUtilities.getWindowAncestor(this), ctx, this::refresh).setVisible(true));

        editButton.setEnabled(false);
        editButton.addActionListener(e -> editSelected());
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelected());

        JButton addButton = Ui.primary("Add book", "plus");
        addButton.addActionListener(e -> addBook());

        rightTools().add(importButton);
        rightTools().add(editButton);
        rightTools().add(deleteButton);
        rightTools().add(addButton);

        Tables.idColumn(table(), 0);
        Tables.widths(table(), 56, 250, 150, 130, 130, 70, 130);
        Tables.statusColumn(table(), 6, BookPanel::statusTone);
        table().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table().getSelectionModel().addListSelectionListener(e -> updateActionState());

        refresh();
    }

    /**
     * Applies an external query (used by the global search box) and
     * reloads the catalog.
     *
     * @param query free-text query matched against title, author and ISBN
     */
    public void applySearch(String query) {
        search.setText(query == null ? "" : query);
        refresh();
    }

    @Override
    protected void reload() {
        String category = (String) categoryFilter.getSelectedItem();
        List<Object[]> rows = new ArrayList<>();
        for (Book book : ctx.bookService.search(search.getText())) {
            boolean matchesCategory = category == null || ALL_CATEGORIES.equals(category)
                    || category.equals(book.getCategoryName());
            if (!matchesCategory) {
                continue;
            }
            int available = book.getAvailableCopies();
            rows.add(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthorName(),
                    book.getCategoryName(),
                    book.getIsbn(),
                    available + " / " + book.getTotalCopies(),
                    available > 0 ? "Available" : "All copies out"});
        }
        setRows(rows, "books");
    }

    private String[] categories() {
        List<String> names = new ArrayList<>();
        names.add(ALL_CATEGORIES);
        for (var category : ctx.bookService.getAllCategories()) {
            if (category.getName() != null && !category.getName().isBlank()) {
                names.add(category.getName());
            }
        }
        return names.toArray(new String[0]);
    }

    private static BadgeTone statusTone(Object value) {
        return "Available".equals(String.valueOf(value)) ? BadgeTone.SUCCESS : BadgeTone.WARNING;
    }

    private void updateActionState() {
        boolean selected = table().getSelectedRow() >= 0;
        editButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private Integer selectedId() {
        int viewRow = table().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return (Integer) tableModel().getValueAt(table().convertRowIndexToModel(viewRow), 0);
    }

    private void addBook() {
        new BookFormDialog(SwingUtilities.getWindowAncestor(this), ctx, null, this::refresh).setVisible(true);
    }

    private void editSelected() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a book to edit.");
            return;
        }
        ctx.bookService.findById(id).ifPresent(book ->
                new BookFormDialog(SwingUtilities.getWindowAncestor(this), ctx, book, this::refresh).setVisible(true));
    }

    private void deleteSelected() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a book to delete.");
            return;
        }
        int viewRow = table().getSelectedRow();
        int modelRow = table().convertRowIndexToModel(viewRow);
        String title = String.valueOf(tableModel().getValueAt(modelRow, 1));
        Object authorValue = tableModel().getValueAt(modelRow, 2);
        String author = authorValue == null ? "" : String.valueOf(authorValue);
        String copy = "\"" + title + "\""
                + (author.isBlank() ? "" : " by " + author);
        if (DialogUtils.confirm(this, "Delete book",
                "Delete " + copy + "? Copies and loan history attached to it stay untouched, "
                        + "but the title will no longer appear in the catalog.",
                "Delete book", true)) {
            try {
                ctx.bookService.deleteBook(id);
                refresh();
            } catch (LibraXException ex) {
                DialogUtils.showError(this, ex.getMessage());
            }
        }
    }
}
