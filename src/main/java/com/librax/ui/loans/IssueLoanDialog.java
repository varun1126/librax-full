package com.librax.ui.loans;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Book;
import com.librax.model.Loan;
import com.librax.model.Member;
import com.librax.ui.components.AppDialog;
import com.librax.ui.components.FormBuilder;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.Window;
import java.util.List;

/**
 * Modal "issue book" form: the member and the title are picked from
 * readable combo boxes instead of raw ids. Eligibility, the borrowing
 * limit, the loan period and unpaid-fine rules are applied by
 * {@code LoanService} when the issue runs — never by this dialog
 * (RULES.md §3).
 */
public class IssueLoanDialog extends AppDialog {

    /**
     * Opens the form.
     *
     * @param owner  owner window for centering; may be {@code null}
     * @param ctx    composition root providing the loan, member and book services
     * @param onDone callback invoked after a successful issue
     */
    public IssueLoanDialog(Window owner, AppContext ctx, Runnable onDone) {
        super(owner, "Issue book");

        JPanel form = body("fillx, ins 16 18 16 18, gap 10");
        FormBuilder fields = new FormBuilder(form);

        JComboBox<Object> memberCombo = fields.comboBox("Member", memberOptions(ctx), true);
        JComboBox<Object> bookCombo = fields.comboBox("Book", bookOptions(ctx), true);
        fields.hint("The loan period, borrowing limit and unpaid-fine rules are applied automatically.");

        JButton cancel = Ui.secondary("Cancel");
        cancel.addActionListener(e -> dispose());

        JButton issue = Ui.primary("Issue book");
        issue.addActionListener(e -> issue(ctx, onDone, memberCombo, bookCombo));

        footerRight(cancel);
        footerRight(issue);
        setDefaultButton(issue);
    }

    /**
     * Validates the selection, issues the book and closes the dialog.
     *
     * @param ctx         composition root providing the loan service
     * @param onDone      callback invoked after a successful issue
     * @param memberCombo member choice control
     * @param bookCombo   book choice control
     */
    private void issue(AppContext ctx, Runnable onDone,
                       JComboBox<Object> memberCombo, JComboBox<Object> bookCombo) {
        Option member = requireOption(memberCombo, "Select the member this book is issued to.");
        if (member == null) {
            return;
        }
        Option book = requireOption(bookCombo, "Select the book to issue.");
        if (book == null) {
            return;
        }
        try {
            Loan loan = ctx.loanService.issueBook(member.id(), book.id());
            // The toast attaches to the owner window so it outlives this dialog.
            DialogUtils.showInfo(getOwner() == null ? this : getOwner(),
                    "Book issued. Due date: " + loan.getDueDate());
            onDone.run();
            dispose();
        } catch (LibraXException ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    /**
     * Resolves the current selection to an option.
     *
     * @param combo  choice control holding {@link Option} entries
     * @param prompt validation message shown while the placeholder is selected
     * @return the selected option, or {@code null} after reporting the problem
     */
    private Option requireOption(JComboBox<Object> combo, String prompt) {
        if (combo.getSelectedItem() instanceof Option option && option.id() >= 0) {
            return option;
        }
        DialogUtils.showError(this, prompt);
        combo.requestFocusInWindow();
        return null;
    }

    /**
     * Builds readable member entries: {@code "Name — membership #"}.
     *
     * @param ctx composition root providing the member service
     * @return combo entries: a placeholder first, then one option per member
     */
    private static Object[] memberOptions(AppContext ctx) {
        List<Member> members = ctx.memberService.findAll();
        Object[] options = new Object[members.size() + 1];
        options[0] = new Option(-1, "Select a member…");
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            String number = text(member.getMembershipNumber());
            options[i + 1] = new Option(member.getId() == null ? -1 : member.getId(),
                    text(member.getFullName()) + (number.isBlank() ? "" : " — " + number));
        }
        return options;
    }

    /**
     * Builds readable book entries: {@code "Title — author"}.
     *
     * @param ctx composition root providing the book service
     * @return combo entries: a placeholder first, then one option per book
     */
    private static Object[] bookOptions(AppContext ctx) {
        List<Book> books = ctx.bookService.search("");
        Object[] options = new Object[books.size() + 1];
        options[0] = new Option(-1, "Select a book…");
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            String author = text(book.getAuthorName());
            options[i + 1] = new Option(book.getId() == null ? -1 : book.getId(),
                    text(book.getTitle()) + (author.isBlank() ? "" : " — " + author));
        }
        return options;
    }

    /** Renders a possibly {@code null} value as display text. */
    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Readable combo-box entry: shows {@code label} while carrying the
     * underlying entity id, so ids never surface in the UI.
     *
     * @param id    entity id, or {@code -1} for the placeholder entry
     * @param label text shown in the closed and open combo states
     */
    private record Option(int id, String label) {

        @Override
        public String toString() {
            return label;
        }
    }
}
