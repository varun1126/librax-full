package com.librax.ui.loans;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Loan;
import com.librax.ui.components.BadgeTone;
import com.librax.ui.components.TablePage;
import com.librax.ui.components.Tables;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Circulation screen: issue and return books over a searchable, filterable
 * loan list with status chips and the fine owed on each loan.
 *
 * <p>Eligibility, loan-period and fine rules belong to {@code LoanService};
 * this panel only collects input and renders results (RULES.md §3).</p>
 */
public class LoanPanel extends TablePage {

    /** Sentinel filter value that disables status filtering. */
    private static final String ALL_STATUSES = "All";

    private final AppContext ctx;
    private final JTextField search;
    private final JComboBox<String> statusFilter;
    private final JButton returnButton = Ui.secondary("Return book", "check");

    /**
     * Builds the page and performs the first load.
     *
     * @param ctx composition root providing the loan, member and book services
     */
    public LoanPanel(AppContext ctx) {
        super("ID", "Book", "Member", "Issued", "Due", "Returned", "Status", "Fine");
        this.ctx = ctx;

        configureEmpty("loans", "No loans match this view",
                "Change the filter, or issue a book to get started.");
        configureEmptyAction("Issue book", this::issueBook);

        search = addSearch("Search title or member…", 220);

        statusFilter = Ui.select(ALL_STATUSES, "ACTIVE", "OVERDUE", "RETURNED");
        statusFilter.addActionListener(e -> refresh());
        leftTools().add(statusFilter);

        returnButton.setEnabled(false);
        returnButton.addActionListener(e -> returnSelected());

        JButton refreshButton = Ui.ghost("Refresh");
        refreshButton.addActionListener(e -> refresh());

        JButton issueButton = Ui.primary("Issue book", "loans");
        issueButton.addActionListener(e -> issueBook());

        rightTools().add(refreshButton);
        rightTools().add(returnButton);
        rightTools().add(issueButton);

        Tables.idColumn(table(), 0);
        Tables.widths(table(), 56, 240, 150, 105, 105, 105, 115, 90);
        Tables.statusColumn(table(), 6, LoanPanel::statusTone);
        Tables.rightAlign(table(), 7);
        table().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table().getSelectionModel().addListSelectionListener(e -> updateActionState());

        refresh();
    }

    /**
     * Loads all loans and applies the status filter and the free-text
     * search (book title or member name) before pushing the rows.
     */
    @Override
    protected void reload() {
        String status = (String) statusFilter.getSelectedItem();
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        List<Object[]> rows = new ArrayList<>();
        for (Loan loan : ctx.loanService.findAll()) {
            String loanStatus = text(loan.getStatus());
            boolean matchesStatus = status == null || ALL_STATUSES.equals(status)
                    || status.equals(loanStatus);
            if (!matchesStatus || (!query.isEmpty() && !matchesQuery(loan, query))) {
                continue;
            }
            rows.add(new Object[]{
                    loan.getId(),
                    text(loan.getBookTitle()),
                    text(loan.getMemberName()),
                    text(loan.getIssueDate()),
                    text(loan.getDueDate()),
                    text(loan.getReturnDate()),
                    loanStatus,
                    fine(loan.getFineAmount())});
        }
        setRows(rows, "loans");
    }

    // ------------------------------------------------------------ row actions

    /** Opens the issue-book dialog, reloading this list on success. */
    private void issueBook() {
        new IssueLoanDialog(SwingUtilities.getWindowAncestor(this), ctx, this::refresh)
                .setVisible(true);
    }

    /** Returns the selected loan, reporting any service failure to the user. */
    private void returnSelected() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a loan to return.");
            return;
        }
        try {
            Loan loan = ctx.loanService.returnBook(id);
            String message = "Book returned.";
            if (loan.getFineAmount() != null && loan.getFineAmount().signum() > 0) {
                message += " Overdue fine: ₹" + loan.getFineAmount();
            }
            DialogUtils.showInfo(this, message);
            refresh();
        } catch (LibraXException ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    // --------------------------------------------------------------- helpers

    /** Enables the return action only while a row is selected. */
    private void updateActionState() {
        returnButton.setEnabled(table().getSelectedRow() >= 0);
    }

    /**
     * Resolves the selected row to its loan id.
     *
     * @return the loan id, or {@code null} when no row is selected
     */
    private Integer selectedId() {
        int viewRow = table().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return (Integer) tableModel().getValueAt(table().convertRowIndexToModel(viewRow), 0);
    }

    /** Matches the search query against the book title or the member name. */
    private static boolean matchesQuery(Loan loan, String query) {
        return containsIgnoreCase(loan.getBookTitle(), query)
                || containsIgnoreCase(loan.getMemberName(), query);
    }

    private static boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(query);
    }

    /** Renders a fine amount as currency, or blank when no fine is recorded. */
    private static String fine(BigDecimal amount) {
        return amount == null ? "" : "₹" + amount;
    }

    /** Renders a possibly {@code null} model value as table text. */
    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Maps a loan status to its badge tone.
     *
     * @param value raw status value rendered in the cell
     * @return {@link BadgeTone#INFO} while a loan is out,
     *         {@link BadgeTone#SUCCESS} once returned and
     *         {@link BadgeTone#DANGER} when overdue or lost
     */
    private static BadgeTone statusTone(Object value) {
        return switch (String.valueOf(value)) {
            case "ACTIVE" -> BadgeTone.INFO;
            case "RETURNED" -> BadgeTone.SUCCESS;
            case "OVERDUE" -> BadgeTone.DANGER;
            case "LOST" -> BadgeTone.DANGER;
            default -> BadgeTone.DANGER;
        };
    }
}
