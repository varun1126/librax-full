package com.librax.ui.books;

import com.librax.AppContext;
import com.librax.api.GoogleBooksService;
import com.librax.dto.GoogleBookDto;
import com.librax.exception.ApiException;
import com.librax.exception.LibraXException;
import com.librax.model.Book;
import com.librax.ui.components.AppDialog;
import com.librax.ui.components.Ui;
import com.librax.ui.theme.Theme;
import com.librax.util.DialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;

/**
 * Google Books search + import dialog. The HTTP call always runs on a
 * {@link SwingWorker} thread — never on the Event Dispatch Thread.
 */
public class GoogleBooksSearchDialog extends AppDialog {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksSearchDialog.class);

    private final AppContext ctx;
    private final DefaultListModel<GoogleBookDto> listModel = new DefaultListModel<>();
    private final JList<GoogleBookDto> resultsList = new JList<>(listModel);
    private final JLabel statusLabel = Ui.caption("Enter a query to search Google Books.");
    private final JButton searchButton = Ui.primary("Search", "search");
    private final JComboBox<GoogleBooksService.SearchType> typeCombo =
            Ui.select(GoogleBooksService.SearchType.values());

    /**
     * Opens the dialog.
     *
     * @param owner      owner window for centering
     * @param ctx        composition root providing the book service
     * @param onImported callback invoked after a successful import
     */
    public GoogleBooksSearchDialog(Window owner, AppContext ctx, Runnable onImported) {
        super(owner, "Import from Google Books");
        this.ctx = ctx;

        JPanel form = body("fillx, ins 16 18 14 18, gap 10");

        JTextField queryField = Ui.searchField("ISBN, title, author or keyword", 320);
        form.add(queryField, "growx");
        form.add(typeCombo, "w 140!");
        form.add(searchButton, "wrap");

        resultsList.setFixedCellHeight(46);
        resultsList.setBackground(Theme.surface());
        resultsList.setSelectionBackground(Theme.selection());
        resultsList.setSelectionForeground(Theme.textPrimary());
        resultsList.setCellRenderer(new ResultRenderer());
        JScrollPane scroll = new JScrollPane(resultsList);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.border(), 1));
        scroll.getViewport().setBackground(Theme.surface());
        scroll.setPreferredSize(new Dimension(10, 300));
        form.add(scroll, "grow, wrap");

        JButton cancelButton = Ui.secondary("Cancel");
        cancelButton.addActionListener(e -> dispose());
        JButton importButton = Ui.primary("Import to library", "plus");
        importButton.addActionListener(e -> importSelected(onImported));

        footerLeft(statusLabel);
        footerRight(cancelButton);
        footerRight(importButton);
        setDefaultButton(searchButton);

        Runnable runSearch = () -> runSearch(queryField.getText(),
                (GoogleBooksService.SearchType) typeCombo.getSelectedItem());
        searchButton.addActionListener(e -> runSearch.run());
        queryField.addActionListener(e -> runSearch.run());
    }

    private void runSearch(String query, GoogleBooksService.SearchType type) {
        if (query == null || query.isBlank()) {
            statusLabel.setText("Enter a search term.");
            return;
        }
        statusLabel.setText("Searching Google Books…");
        searchButton.setEnabled(false);
        listModel.clear();

        new SwingWorker<List<GoogleBookDto>, Void>() {
            @Override
            protected List<GoogleBookDto> doInBackground() {
                return ctx.googleBooksService.search(query, type);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                try {
                    List<GoogleBookDto> results = get();
                    for (GoogleBookDto dto : results) {
                        listModel.addElement(dto);
                    }
                    statusLabel.setText(results.isEmpty()
                            ? "No results found."
                            : results.size() + " result" + (results.size() == 1 ? "" : "s") + ".");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.warn("Google Books search failed", cause);
                    String friendly = cause instanceof ApiException ? cause.getMessage()
                            : "Unable to search Google Books right now. Check your connection and retry.";
                    statusLabel.setText(friendly);
                }
            }
        }.execute();
    }

    private void importSelected(Runnable onImported) {
        GoogleBookDto selected = resultsList.getSelectedValue();
        if (selected == null) {
            DialogUtils.showError(this, "Select a result to import.");
            return;
        }
        try {
            Book book = new Book();
            book.setTitle(selected.getTitle());
            book.setSubtitle(selected.getSubtitle());
            book.setPublisher(selected.getPublisher());
            book.setDescription(selected.getDescription());
            book.setPageCount(selected.getPageCount());
            book.setCoverImageUrl(selected.getThumbnailUrl());
            book.setIsbn(selected.getIsbn() != null ? selected.getIsbn()
                    : ("NOISBN-" + System.nanoTime() % 1000000));
            if (selected.getAuthorNames() != null) {
                book.setAuthorId(ctx.bookService.resolveAuthor(selected.getAuthorNames()).getId());
                book.setAuthorName(selected.getAuthorNames());
            }
            if (selected.getCategory() != null) {
                book.setCategoryId(ctx.bookService.resolveCategory(selected.getCategory()).getId());
                book.setCategoryName(selected.getCategory());
            }
            ctx.bookService.createBook(book, 1);
            DialogUtils.showInfo(this, "Imported “" + selected.getTitle() + "” into the catalog.");
            onImported.run();
            dispose();
        } catch (LibraXException ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    /** Two-line result row: title over author / publisher metadata. */
    private static final class ResultRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value instanceof GoogleBookDto dto) {
                String title = escape(dto.getTitle());
                String meta = escape(meta(dto));
                label.setText("<html><b>" + title + "</b><br><span style='color:#"
                        + hex(Theme.textSecondary()) + ";'>" + meta + "</span></html>");
            }
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            return label;
        }

        private static String meta(GoogleBookDto dto) {
            StringBuilder sb = new StringBuilder();
            if (dto.getAuthorNames() != null && !dto.getAuthorNames().isBlank()) {
                sb.append(dto.getAuthorNames());
            }
            if (dto.getPublisher() != null && !dto.getPublisher().isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(" · ");
                }
                sb.append(dto.getPublisher());
            }
            if (dto.getPublishedDate() != null && !dto.getPublishedDate().isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(" · ");
                }
                sb.append(dto.getPublishedDate());
            }
            return sb.isEmpty() ? "Metadata unavailable" : sb.toString();
        }

        private static String escape(String text) {
            if (text == null) {
                return "";
            }
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        private static String hex(java.awt.Color color) {
            return String.format("%06X", color.getRGB() & 0xFFFFFF);
        }
    }
}
