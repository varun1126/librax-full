package com.librax.ui.reports;

import com.librax.AppContext;
import com.librax.exception.LibraXException;
import com.librax.util.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class ReportPanel extends JPanel {

    private final AppContext ctx;

    public ReportPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new GridLayout(0, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        addReportButton("Export All Books (CSV)", this::exportBooks);
        addReportButton("Export Overdue Books (CSV)", this::exportOverdue);
        addReportButton("Export Members (CSV)", this::exportMembers);
        addReportButton("Export Fine Collection (CSV)", this::exportFines);
    }

    private void addReportButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setMaximumSize(new Dimension(300, 40));
        button.addActionListener(e -> runInBackground(action));
        add(button);
    }

    private void runInBackground(Runnable action) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { action.run(); return null; }
        }.execute();
    }

    private Path chooseFile(String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        int result = chooser.showSaveDialog(this);
        return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile().toPath() : null;
    }

    private void exportBooks() {
        Path p = chooseFile("books.csv");
        if (p == null) return;
        try { ctx.reportService.exportAllBooksCsv(p); DialogUtils.showInfo(this, "Exported to " + p); }
        catch (LibraXException ex) { DialogUtils.showError(this, ex.getMessage()); }
    }

    private void exportOverdue() {
        Path p = chooseFile("overdue-books.csv");
        if (p == null) return;
        try { ctx.reportService.exportOverdueBooksCsv(p); DialogUtils.showInfo(this, "Exported to " + p); }
        catch (LibraXException ex) { DialogUtils.showError(this, ex.getMessage()); }
    }

    private void exportMembers() {
        Path p = chooseFile("members.csv");
        if (p == null) return;
        try { ctx.reportService.exportMembersCsv(p); DialogUtils.showInfo(this, "Exported to " + p); }
        catch (LibraXException ex) { DialogUtils.showError(this, ex.getMessage()); }
    }

    private void exportFines() {
        Path p = chooseFile("fines.csv");
        if (p == null) return;
        try { ctx.reportService.exportFineCollectionCsv(p); DialogUtils.showInfo(this, "Exported to " + p); }
        catch (LibraXException ex) { DialogUtils.showError(this, ex.getMessage()); }
    }
}
