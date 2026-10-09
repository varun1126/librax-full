package com.librax.ui.members;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Member;
import com.librax.ui.components.AppDialog;
import com.librax.ui.components.FormBuilder;
import com.librax.ui.components.Ui;
import com.librax.util.DialogUtils;
import com.librax.util.ValidationUtils;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Window;

/**
 * Add/edit form for a library member: label-above-field layout, inline
 * required-field markers and a right-aligned action footer.
 *
 * <p>The membership number is editable only while creating; existing
 * members keep their number. Saves go through {@code MemberService} and
 * surface failures via {@link DialogUtils#showError}.</p>
 */
public class MemberFormDialog extends AppDialog {

    /**
     * Opens the form.
     *
     * @param owner    owner window for centering
     * @param ctx      composition root providing the member service
     * @param existing member being edited, or {@code null} to create one
     * @param onSaved  callback invoked after a successful save
     */
    public MemberFormDialog(Window owner, AppContext ctx, Member existing, Runnable onSaved) {
        super(owner, existing == null ? "Add member" : "Edit member");

        boolean creating = existing == null;
        JPanel form = body("fillx, ins 16 18 16 18, gap 10");
        FormBuilder fields = new FormBuilder(form);

        JTextField numberField = fields.textField("Membership #",
                existing != null ? text(existing.getMembershipNumber())
                        : "M" + (System.currentTimeMillis() % 100000), true);
        numberField.setEditable(creating);
        JTextField nameField = fields.textField("Full name",
                text(existing == null ? null : existing.getFullName()), true);
        JTextField emailField = fields.textField("Email",
                text(existing == null ? null : existing.getEmail()), true);
        JTextField phoneField = fields.textField("Phone",
                text(existing == null ? null : existing.getPhone()), true);
        JTextField addressField = fields.textField("Address",
                text(existing == null ? null : existing.getAddress()), false);
        fields.hint(creating
                ? "A membership number is suggested; adjust it before saving."
                : "The membership number is fixed once the member exists.");

        JButton cancel = Ui.secondary("Cancel");
        cancel.addActionListener(e -> dispose());

        JButton save = Ui.primary("Save member");
        save.addActionListener(e -> save(ctx, existing, onSaved, numberField,
                nameField, emailField, phoneField, addressField));

        footerRight(cancel);
        footerRight(save);
        setDefaultButton(save);
    }

    /**
     * Validates the fields, then creates or updates the member.
     *
     * @param ctx         composition root providing the member service
     * @param existing    member being edited, or {@code null} to create one
     * @param onSaved     callback invoked after a successful save
     * @param numberField membership number field
     * @param nameField   full name field
     * @param emailField  email field
     * @param phoneField  phone field
     * @param addressField address field (optional)
     */
    private void save(AppContext ctx, Member existing, Runnable onSaved,
                      JTextField numberField, JTextField nameField, JTextField emailField,
                      JTextField phoneField, JTextField addressField) {
        if (numberField.getText().trim().isBlank()) {
            DialogUtils.showError(this, "Membership number is required.");
            numberField.requestFocusInWindow();
            return;
        }
        if (nameField.getText().trim().isBlank()) {
            DialogUtils.showError(this, "Full name is required.");
            nameField.requestFocusInWindow();
            return;
        }
        if (ValidationUtils.isBlank(emailField.getText())) {
            DialogUtils.showError(this, "Email is required.");
            emailField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            DialogUtils.showError(this,
                    "Enter a valid email address, e.g. name@example.org.");
            emailField.requestFocusInWindow();
            return;
        }
        if (ValidationUtils.isBlank(phoneField.getText())) {
            DialogUtils.showError(this, "Phone is required.");
            phoneField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtils.isValidPhone(phoneField.getText())) {
            DialogUtils.showError(this,
                    "Enter a valid phone number (7–15 digits; spaces, brackets and + are allowed).");
            phoneField.requestFocusInWindow();
            return;
        }
        try {
            Member member = existing != null ? existing : new Member();
            member.setMembershipNumber(numberField.getText().trim());
            member.setFullName(nameField.getText().trim());
            member.setEmail(emailField.getText().trim());
            member.setPhone(phoneField.getText().trim());
            member.setAddress(addressField.getText().trim());

            if (existing == null) {
                ctx.memberService.createMember(member);
            } else {
                ctx.memberService.updateMember(member);
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
}
