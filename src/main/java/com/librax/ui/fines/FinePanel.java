package com.librax.ui.fines;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.model.Fine;
import com.librax.model.PaymentMethod;
import com.librax.util.DialogUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class FinePanel extends JPanel {

    private final AppContext ctx;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Member", "Amount", "Paid", "Outstanding", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public FinePanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton payButton = new JButton("Record Payment");
        JButton refreshButton = new JButton("Refresh");
        toolbar.add(payButton);
        toolbar.add(refreshButton);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        payButton.addActionListener(e -> recordPayment());
        refreshButton.addActionListener(e -> refresh());

        refresh();
    }

    private void recordPayment() {
        int row = table.getSelectedRow();
        if (row < 0) { DialogUtils.showError(this, "Select a fine to pay."); return; }
        int fineId = (int) model.getValueAt(row, 0);

        JTextField amountField = new JTextField();
        JComboBox<PaymentMethod> methodCombo = new JComboBox<>(PaymentMethod.values());
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Amount")); form.add(amountField);
        form.add(new JLabel("Method")); form.add(methodCombo);

        int result = JOptionPane.showConfirmDialog(this, form, "Record Payment", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            ctx.fineService.recordPayment(fineId, amount, (PaymentMethod) methodCombo.getSelectedItem());
            refresh();
        } catch (NumberFormatException nfe) {
            DialogUtils.showError(this, "Enter a valid amount.");
        } catch (LibraXException ex) {
            DialogUtils.showError(this, ex.getMessage());
        }
    }

    public void refresh() {
        model.setRowCount(0);
        List<Fine> fines = ctx.fineService.findAll();
        for (Fine f : fines) {
            model.addRow(new Object[]{f.getId(), f.getMemberName(), f.getAmount(), f.getAmountPaid(),
                    f.getOutstanding(), f.getStatus()});
        }
    }
}
