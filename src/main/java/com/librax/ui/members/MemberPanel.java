package com.librax.ui.members;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Member;
import com.librax.ui.components.BadgeTone;
import com.librax.ui.components.TablePage;
import com.librax.ui.components.Tables;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Members screen: searchable, status-filterable member list with row
 * actions (edit / deactivate) and the add-member primary action.
 *
 * <p>Rows are loaded from {@code MemberService}; the dialog-based add/edit
 * form lives in {@link MemberFormDialog}.</p>
 */
public class MemberPanel extends TablePage {

    private static final String ALL_STATUSES = "All statuses";

    private final AppContext ctx;
    private final JTextField search;
    private final JComboBox<String> statusFilter;
    private final JButton editButton = Ui.secondary("Edit", "edit");
    private final JButton deactivateButton = Ui.secondary("Deactivate", "trash");

    /**
     * Builds the page and performs the first load.
     *
     * @param ctx composition root providing the member service
     */
    public MemberPanel(AppContext ctx) {
        super("ID", "Membership #", "Name", "Email", "Phone", "Status", "Registered");
        this.ctx = ctx;

        configureEmpty("users", "No members match your search",
                "Adjust the filters, or register a new member.");
        configureEmptyAction("Add member", this::addMember);

        search = addSearch("Search name, email or membership #…", 240);

        statusFilter = Ui.select(ALL_STATUSES, "ACTIVE", "EXPIRED", "SUSPENDED");
        statusFilter.addActionListener(e -> refresh());
        leftTools().add(statusFilter);

        editButton.setEnabled(false);
        editButton.addActionListener(e -> editSelected());
        deactivateButton.setEnabled(false);
        deactivateButton.addActionListener(e -> deactivateSelected());

        JButton addButton = Ui.primary("Add member", "plus");
        addButton.addActionListener(e -> addMember());

        rightTools().add(editButton);
        rightTools().add(deactivateButton);
        rightTools().add(addButton);

        Tables.idColumn(table(), 0);
        Tables.widths(table(), 56, 130, 190, 230, 130, 120, 110);
        Tables.statusColumn(table(), 5, MemberPanel::statusTone);
        table().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table().getSelectionModel().addListSelectionListener(e -> updateActionState());

        refresh();
    }

    /**
     * Loads members matching the current search text and status filter
     * from the service and pushes them into the table.
     */
    @Override
    protected void reload() {
        String status = (String) statusFilter.getSelectedItem();
        List<Object[]> rows = new ArrayList<>();
        for (Member member : ctx.memberService.search(search.getText())) {
            boolean matchesStatus = status == null || ALL_STATUSES.equals(status)
                    || status.equals(String.valueOf(member.getStatus()));
            if (!matchesStatus) {
                continue;
            }
            rows.add(new Object[]{
                    member.getId(),
                    member.getMembershipNumber(),
                    member.getFullName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getStatus() == null ? "" : member.getStatus().name(),
                    member.getRegistrationDate() == null
                            ? "" : String.valueOf(member.getRegistrationDate())});
        }
        setRows(rows, "members");
    }

    /**
     * Maps a membership status to its badge tone: {@code ACTIVE} reads
     * positive, {@code EXPIRED} needs attention, {@code SUSPENDED} is
     * blocking.
     *
     * @param value raw status value from the table model
     * @return the semantic tone used to render the status chip
     */
    private static BadgeTone statusTone(Object value) {
        return switch (String.valueOf(value)) {
            case "ACTIVE" -> BadgeTone.SUCCESS;
            case "EXPIRED" -> BadgeTone.WARNING;
            case "SUSPENDED" -> BadgeTone.DANGER;
            default -> BadgeTone.NEUTRAL;
        };
    }

    /** Enables the row actions only while a row is selected. */
    private void updateActionState() {
        boolean selected = table().getSelectedRow() >= 0;
        editButton.setEnabled(selected);
        deactivateButton.setEnabled(selected);
    }

    /**
     * @return the model id of the selected row, or {@code null} when no
     * row is selected
     */
    private Integer selectedId() {
        int viewRow = table().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return (Integer) tableModel().getValueAt(table().convertRowIndexToModel(viewRow), 0);
    }

    /**
     * Reads a cell from the selected row as display text.
     *
     * @param modelColumn model column index
     * @return cell text, or an empty string for {@code null} values
     */
    private String selectedText(int modelColumn) {
        int modelRow = table().convertRowIndexToModel(table().getSelectedRow());
        Object value = tableModel().getValueAt(modelRow, modelColumn);
        return value == null ? "" : String.valueOf(value);
    }

    /** Opens the member form in create mode. */
    private void addMember() {
        new MemberFormDialog(SwingUtilities.getWindowAncestor(this), ctx, null, () -> {
            refresh();
            DialogUtils.showInfo(this, "Member registered.");
        }).setVisible(true);
    }

    /** Opens the member form prefilled with the selected member. */
    private void editSelected() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a member to edit.");
            return;
        }
        ctx.memberService.findById(id).ifPresent(member ->
                new MemberFormDialog(SwingUtilities.getWindowAncestor(this), ctx, member, () -> {
                    refresh();
                    DialogUtils.showInfo(this, "Member updated.");
                }).setVisible(true));
    }

    /** Confirms with specific copy, then deactivates the selected member. */
    private void deactivateSelected() {
        Integer id = selectedId();
        if (id == null) {
            DialogUtils.showError(this, "Select a member to deactivate.");
            return;
        }
        String name = selectedText(2);
        String membershipNumber = selectedText(1);
        if (DialogUtils.confirm(this, "Deactivate member",
                "Deactivate " + name + " (" + membershipNumber + ")? They will no longer "
                        + "be able to borrow until reactivated.",
                "Deactivate", true)) {
            try {
                ctx.memberService.deactivateMember(id);
                refresh();
            } catch (LibraXException ex) {
                DialogUtils.showError(this, ex.getMessage());
            }
        }
    }
}
