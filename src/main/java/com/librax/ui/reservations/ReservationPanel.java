package com.librax.ui.reservations;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Reservation;
import com.librax.ui.components.BadgeTone;
import com.librax.ui.components.TablePage;
import com.librax.ui.components.Tables;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Reservations screen: the queue of held titles with a status filter,
 * fulfilment / cancellation actions and a designed empty state.
 *
 * <p>State transitions are performed by {@code ReservationService}; this
 * panel collects the decision, confirms it with the user and renders the
 * outcome (RULES.md §3).</p>
 */
public class ReservationPanel extends TablePage {

    /** Sentinel filter value that disables status filtering. */
    private static final String ALL_STATUSES = "All";

    private final AppContext ctx;
    private final JComboBox<String> statusFilter;
    private final JButton fulfillButton = Ui.secondary("Mark fulfilled", "check");
    private final JButton cancelButton = Ui.secondary("Cancel", "close");

    /**
     * Builds the page and performs the first load.
     *
     * @param ctx composition root providing the reservation service
     */
    public ReservationPanel(AppContext ctx) {
        super("ID", "Book", "Member", "Requested", "Status");
        this.ctx = ctx;

        configureEmpty("reservations", "No reservations in this view",
                "Reservations queue when every copy of a title is on loan.");
        configureEmptyAction("New reservation", this::newReservation);

        statusFilter = Ui.select(ALL_STATUSES, "WAITING", "READY", "FULFILLED", "CANCELLED", "EXPIRED");
        statusFilter.addActionListener(e -> refresh());
        leftTools().add(statusFilter);

        JButton refreshButton = Ui.ghost("Refresh");
        refreshButton.addActionListener(e -> refresh());

        fulfillButton.setEnabled(false);
        fulfillButton.addActionListener(e -> markFulfilled());
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelSelected());

        JButton newButton = Ui.primary("New reservation", "plus");
        newButton.addActionListener(e -> newReservation());

        rightTools().add(refreshButton);
        rightTools().add(fulfillButton);
        rightTools().add(cancelButton);
        rightTools().add(newButton);

        Tables.idColumn(table(), 0);
        Tables.widths(table(), 56, 260, 170, 120, 120);
        Tables.statusColumn(table(), 4, ReservationPanel::statusTone);
        table().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table().getSelectionModel().addListSelectionListener(e -> updateActionState());

        refresh();
    }

    /**
     * Loads all reservations and applies the status filter before pushing
     * the rows.
     */
    @Override
    protected void reload() {
        String status = (String) statusFilter.getSelectedItem();
        List<Object[]> rows = new ArrayList<>();
        for (Reservation reservation : ctx.reservationService.findAll()) {
            String reservationStatus = text(reservation.getStatus());
            boolean matchesStatus = status == null || ALL_STATUSES.equals(status)
                    || status.equals(reservationStatus);
            if (!matchesStatus) {
                continue;
            }
            rows.add(new Object[]{
                    reservation.getId(),
                    text(reservation.getBookTitle()),
                    text(reservation.getMemberName()),
                    text(reservation.getReservationDate()),
                    reservationStatus});
        }
        setRows(rows, "reservations");
    }

    // ------------------------------------------------------------ row actions

    /** Opens the new-reservation dialog, reloading this list on success. */
    private void newReservation() {
        new ReservationDialog(SwingUtilities.getWindowAncestor(this), ctx, this::refresh)
                .setVisible(true);
    }

    /** Confirms, then marks the selected reservation as fulfilled. */
    private void markFulfilled() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a reservation to fulfil.");
            return;
        }
        String message = "Mark the reservation of \"" + selectedText(1) + "\" placed by "
                + selectedText(2) + " as fulfilled? This closes the reservation.";
        if (DialogUtils.confirm(this, "Mark fulfilled", message, "Mark fulfilled", false)) {
            try {
                ctx.reservationService.markFulfilled(id);
                DialogUtils.showInfo(this, "Reservation marked as fulfilled.");
                refresh();
            } catch (LibraXException ex) {
                DialogUtils.showError(this, ex.getMessage());
            }
        }
    }

    /** Confirms destructively, then cancels the selected reservation. */
    private void cancelSelected() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a reservation to cancel.");
            return;
        }
        String message = "Cancel the reservation of \"" + selectedText(1) + "\" placed by "
                + selectedText(2) + "? The reservation will be marked as cancelled and cannot be restored.";
        if (DialogUtils.confirm(this, "Cancel reservation", message, "Cancel reservation", true)) {
            try {
                ctx.reservationService.cancel(id);
                DialogUtils.showInfo(this, "Reservation cancelled.");
                refresh();
            } catch (LibraXException ex) {
                DialogUtils.showError(this, ex.getMessage());
            }
        }
    }

    // --------------------------------------------------------------- helpers

    /** Enables the row actions only while a row is selected. */
    private void updateActionState() {
        boolean selected = table().getSelectedRow() >= 0;
        fulfillButton.setEnabled(selected);
        cancelButton.setEnabled(selected);
    }

    /**
     * Resolves the selected row to its reservation id.
     *
     * @return the reservation id, or {@code null} when no row is selected
     */
    private Integer selectedId() {
        int viewRow = table().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return (Integer) tableModel().getValueAt(table().convertRowIndexToModel(viewRow), 0);
    }

    /**
     * Reads a text cell of the selected row, used to name the title and
     * member in confirmation copy.
     *
     * @param column model column index
     * @return the cell text, or an empty string when nothing is selected
     */
    private String selectedText(int column) {
        int viewRow = table().getSelectedRow();
        if (viewRow < 0) {
            return "";
        }
        return text(tableModel().getValueAt(table().convertRowIndexToModel(viewRow), column));
    }

    /** Renders a possibly {@code null} model value as table text. */
    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Maps a reservation status to its badge tone.
     *
     * @param value raw status value rendered in the cell
     * @return {@link BadgeTone#WARNING} while waiting, {@link BadgeTone#INFO}
     *         when ready, {@link BadgeTone#SUCCESS} once fulfilled,
     *         {@link BadgeTone#NEUTRAL} once cancelled and
     *         {@link BadgeTone#DANGER} when expired
     */
    private static BadgeTone statusTone(Object value) {
        return switch (String.valueOf(value)) {
            case "WAITING" -> BadgeTone.WARNING;
            case "READY" -> BadgeTone.INFO;
            case "FULFILLED" -> BadgeTone.SUCCESS;
            case "CANCELLED" -> BadgeTone.NEUTRAL;
            case "EXPIRED" -> BadgeTone.DANGER;
            default -> BadgeTone.NEUTRAL;
        };
    }
}
