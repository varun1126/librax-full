package com.librax.ui.books;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Book;
import com.librax.ui.components.AppDialog;
import com.librax.ui.components.FormBuilder;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Window;

/**
 * Add/edit form for a book title: label-above-field layout, inline
 * required-field markers and a right-aligned action footer.
 */
public class BookFormDialog extends AppDialog {

    /**
     * Opens the form.
     *
     * @param owner    owner window for centering
     * @param ctx      composition root providing the book service
     * @param existing book being edited, or {@code null} to create one
     * @param onSaved  callback invoked after a successful save
     */
    public BookFormDialog(Window owner, AppContext ctx, Book existing, Runnable onSaved) {
        super(owner, existing == null ? "Add book" : "Edit book");

        JPanel form = body("fillx, ins 16 18 12 18, gap 10");
        FormBuilder fields = new FormBuilder(form);

        JTextField titleField = fields.textField("Title", text(existing == null ? null : existing.getTitle()), true);
        JTextField isbnField = fields.textField("ISBN", text(existing == null ? null : existing.getIsbn()), true);
        JTextField authorField = fields.textField("Author", text(existing == null ? null : existing.getAuthorName()), false);
        JTextField categoryField = fields.textField("Category", text(existing == null ? null : existing.getCategoryName()), false);
        JTextField publisherField = fields.textField("Publisher", text(existing == null ? null : existing.getPublisher()), false);
        JTextField shelfField = fields.textField("Shelf location", text(existing == null ? null : existing.getShelfLocation()), false);
        JTextField copiesField = fields.textField("Copies to add", existing == null ? "1" : "0", false);
        fields.hint(existing == null
                ? "Author and category are matched to existing records, or created when new."
                : "Extra copies are added to the existing stock of this title.");

        JButton cancel = Ui.secondary("Cancel");
        cancel.addActionListener(e -> dispose());

        JButton save = Ui.primary(existing == null ? "Add book" : "Save changes");
        save.addActionListener(e -> save(ctx, existing, onSaved, titleField, isbnField,
                authorField, categoryField, publisherField, shelfField, copiesField));

        footerRight(cancel);
        footerRight(save);
        setDefaultButton(save);
    }

    private void save(AppContext ctx, Book existing, Runnable onSaved,
                      JTextField titleField, JTextField isbnField, JTextField authorField,
                      JTextField categoryField, JTextField publisherField, JTextField shelfField,
                      JTextField copiesField) {
        if (titleField.getText().trim().isBlank()) {
            DialogUtils.showError(this, "Title is required.");
            titleField.requestFocusInWindow();
            return;
        }
        if (isbnField.getText().trim().isBlank()) {
            DialogUtils.showError(this, "ISBN is required.");
            isbnField.requestFocusInWindow();
            return;
        }
        try {
            Book book = existing != null ? existing : new Book();
            book.setTitle(titleField.getText().trim());
            book.setIsbn(isbnField.getText().trim());
            book.setPublisher(publisherField.getText().trim());
            book.setShelfLocation(shelfField.getText().trim());
            if (!authorField.getText().isBlank()) {
                book.setAuthorId(ctx.bookService.resolveAuthor(authorField.getText().trim()).getId());
                book.setAuthorName(authorField.getText().trim());
            }
            if (!categoryField.getText().isBlank()) {
                book.setCategoryId(ctx.bookService.resolveCategory(categoryField.getText().trim()).getId());
                book.setCategoryName(categoryField.getText().trim());
            }
            int copies = parseIntSafe(copiesField.getText(), 0);

            if (existing == null) {
                ctx.bookService.createBook(book, Math.max(0, copies));
            } else {
                ctx.bookService.updateBook(book);
                for (int i = 0; i < copies; i++) {
                    ctx.bookService.addCopy(book.getId());
                }
            }
            onSaved.run();
            dispose();
        } catch (LibraXException ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
