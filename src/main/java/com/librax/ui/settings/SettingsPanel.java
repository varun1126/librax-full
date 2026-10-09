package com.librax.ui.settings;

import com.librax.AppContext;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    public SettingsPanel(AppContext ctx) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0; gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        add(new JLabel("Library name: " + ctx.appConfig.getLibraryName()), gc);
        gc.gridy++;
        add(new JLabel("Loan period (days): " + ctx.appConfig.getLoanPeriodDays()), gc);
        gc.gridy++;
        add(new JLabel("Max books per member: " + ctx.appConfig.getMaxBooksPerMember()), gc);
        gc.gridy++;
        add(new JLabel("Fine per day: ₹" + ctx.appConfig.getFinePerDay()), gc);
        gc.gridy++;
        add(new JLabel("Database file: " + ctx.databaseConfig.getDatabaseFile()), gc);
        gc.gridy++;
        add(new JLabel("<html><i>Edit application.properties and restart to change these values.</i></html>"), gc);
    }
}
