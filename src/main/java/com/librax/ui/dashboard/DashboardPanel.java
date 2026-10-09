package com.librax.ui.dashboard;

import com.librax.AppContext;
import com.librax.model.Loan;
import com.librax.ui.components.CardPanel;
import com.librax.ui.components.DashboardCard;
import com.librax.ui.components.EmptyState;
import com.librax.ui.components.Tables;
import com.librax.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Dashboard: KPI tiles, two charts and two operational lists (recent
 * issues and overdue loans) — the daily briefing a librarian opens the
 * application to see.
 */
@SuppressWarnings("null")
public class DashboardPanel extends JPanel {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final AppContext ctx;
    private final JPanel kpiGrid = new JPanel(new java.awt.GridLayout(2, 4, 12, 12));
    private final JPanel chartsRow = new JPanel(new java.awt.GridLayout(1, 2, 16, 16));
    private final JPanel listsRow = new JPanel(new java.awt.GridLayout(1, 2, 16, 16));

    private final DashboardCard titlesCard = new DashboardCard("Titles in catalog", "-");
    private final DashboardCard copiesCard = new DashboardCard("Copies available", "-");
    private final DashboardCard loansCard = new DashboardCard("Books on loan", "-");
    private final DashboardCard overdueCard = new DashboardCard("Overdue loans", "-");
    private final DashboardCard membersCard = new DashboardCard("Active members", "-");
    private final DashboardCard reservationsCard = new DashboardCard("Reservations waiting", "-");
    private final DashboardCard finesCard = new DashboardCard("Outstanding fines", "-");
    private final DashboardCard todayCard = new DashboardCard("Today's circulation", "-");

    /**
     * Builds the dashboard and performs the first data load.
     *
     * @param ctx composition root providing the read-only analytics services
     */
    public DashboardPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        setBackground(Theme.workspace());

        for (DashboardCard card : new DashboardCard[]{
                titlesCard, copiesCard, loansCard, overdueCard,
                membersCard, reservationsCard, finesCard, todayCard}) {
            kpiGrid.add(card);
        }
        kpiGrid.setOpaque(false);
        chartsRow.setOpaque(false);
        listsRow.setOpaque(false);

        add(kpiGrid, BorderLayout.NORTH);
        add(chartsRow, BorderLayout.CENTER);
        add(listsRow, BorderLayout.SOUTH);

        refresh();
    }

    /** Reloads every KPI, chart and list from the service layer. */
    public void refresh() {
        var a = ctx.analyticsService;

        titlesCard.setValue(String.valueOf(a.totalBooks()));
        titlesCard.setFoot("catalogued titles");

        copiesCard.setValue(String.valueOf(a.totalAvailableCopies()));
        copiesCard.setFoot("ready to lend");

        long active = a.issuedBooksCount();
        loansCard.setValue(String.valueOf(active));
        loansCard.setFoot("currently on loan");

        long overdue = a.overdueBooksCount();
        overdueCard.setValue(String.valueOf(overdue));
        overdueCard.setFoot(overdue > 0 ? "past their due date" : "nothing past due");

        membersCard.setValue(String.valueOf(a.totalMembers()));
        membersCard.setFoot("registered patrons");

        long waiting = a.activeReservationsCount();
        reservationsCard.setValue(String.valueOf(waiting));
        reservationsCard.setFoot("waiting or ready");

        BigDecimal fines = a.outstandingFinesTotal();
        finesCard.setValue("₹" + stripTrailingZeros(fines));
        finesCard.setFoot("unpaid balance");

        long today = a.todaysTransactionsCount();
        todayCard.setValue(String.valueOf(today));
        todayCard.setFoot("issues and returns today");

        chartsRow.removeAll();
        chartsRow.add(chartCard("Circulation trend", "issues per month",
                new VerticalBars(a.monthlyIssueStats(6))));
        chartsRow.add(chartCard("Most borrowed titles", "all time",
                new HorizontalBars(a.mostBorrowedBooks(7))));

        listsRow.removeAll();
        listsRow.add(recentIssuesCard());
        listsRow.add(overdueCard());

        revalidate();
        repaint();
    }

    private static String stripTrailingZeros(BigDecimal value) {
        BigDecimal stripped = value == null ? BigDecimal.ZERO : value.stripTrailingZeros();
        return stripped.toPlainString();
    }

    private CardPanel chartCard(String title, String subtitle, JPanel chart) {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));

        chart.setPreferredSize(new Dimension(10, 200));
        body.add(chart, BorderLayout.CENTER);

        JLabel hint = new JLabel(subtitle);
        hint.setFont(Theme.caption());
        hint.setForeground(Theme.textMuted());
        body.add(hint, BorderLayout.NORTH);

        CardPanel card = CardPanel.titled(title, body);
        card.setPreferredSize(new Dimension(10, 250));
        return card;
    }

    // ------------------------------------------------------------------ lists

    private CardPanel recentIssuesCard() {
        DefaultTableModel model = Tables.model("Title", "Member", "Due");
        List<Loan> loans = new ArrayList<>(ctx.loanService.findAll());
        loans.sort(Comparator.comparing(l -> l.getIssueDate(),
                Comparator.nullsLast(Comparator.reverseOrder())));
        int rows = 0;
        for (Loan loan : loans) {
            if (rows >= 6) {
                break;
            }
            model.addRow(new Object[]{
                    loan.getBookTitle(),
                    loan.getMemberName(),
                    loan.getDueDate() == null ? "" : DATE.format(loan.getDueDate())});
            rows++;
        }
        return listCard("Recently issued", model, rows, "calendar", "No books issued yet",
                "Issues appear here as soon as circulation starts.");
    }

    private CardPanel overdueCard() {
        DefaultTableModel model = Tables.model("Title", "Member", "Days late");
        List<Loan> overdue = new ArrayList<>(ctx.loanService.findOverdue());
        int rows = 0;
        LocalDate today = LocalDate.now();
        for (Loan loan : overdue) {
            if (rows >= 6) {
                break;
            }
            long days = loan.getDueDate() == null ? 0
                    : java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), today);
            model.addRow(new Object[]{loan.getBookTitle(), loan.getMemberName(), Math.max(0, days)});
            rows++;
        }
        CardPanel card = listCard("Overdue loans", model, rows, "clock", "No overdue loans",
                "Everything is within its loan period.");
        return card;
    }

    private CardPanel listCard(String title, DefaultTableModel model, int rowCount,
                               String emptyIcon, String emptyMessage, String emptyHint) {
        JTable table = new JTable(model);
        Tables.style(table);
        Tables.noHeader(table);
        table.setRowSelectionAllowed(true);
        if (model.getColumnCount() == 3) {
            Tables.rightAlign(table, 2);
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.surface());
        scroll.setPreferredSize(new Dimension(10, 150));

        javax.swing.JComponent body;
        if (rowCount == 0) {
            EmptyState empty = new EmptyState();
            empty.setState(emptyIcon, emptyMessage, emptyHint);
            empty.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
            body = empty;
        } else {
            body = scroll;
        }
        body.setOpaque(false);

        CardPanel card = CardPanel.titled(title, wrap(body));
        card.setPreferredSize(new Dimension(10, 210));
        return card;
    }

    private JPanel wrap(javax.swing.JComponent body) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        holder.add(body, BorderLayout.CENTER);
        return holder;
    }    // ----------------------------------------------------------------- charts

    /** Horizontal bar chart for ranked categorical data. */
    private static final class HorizontalBars extends JPanel {

        private final Map<String, Long> data;

        private HorizontalBars(Map<String, Long> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (data == null || data.isEmpty()) {
                g2.setColor(Theme.textMuted());
                g2.setFont(Theme.caption());
                g2.drawString("No circulation recorded yet", 4, 20);
                g2.dispose();
                return;
            }

            int labelWidth = 150;
            int valueWidth = 40;
            int right = 6;
            int barX = labelWidth + 8;
            int barArea = Math.max(40, getWidth() - barX - valueWidth - right - 6);
            long max = data.values().stream().mapToLong(v -> v != null ? v : 0L).max().orElse(1);

            int rowH = Math.max(20, Math.min(30, (getHeight() - 6) / data.size()));
            int y = 4;
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                String label = entry.getKey() == null ? "—" : entry.getKey();
                g2.setFont(Theme.caption());
                FontMetrics fm = g2.getFontMetrics();

                g2.setColor(Theme.textSecondary());
                g2.drawString(truncate(g2, label, labelWidth - 6), 2, y + rowH / 2 + fm.getAscent() / 2 - 3);

                int barH = 9;
                int barY = y + (rowH - barH) / 2;
                g2.setColor(Theme.neutralSubtle());
                g2.fillRoundRect(barX, barY, barArea, barH, 4, 4);

                int fill = (int) Math.max(4, barArea * (entry.getValue() / (double) max));
                g2.setColor(Theme.accent());
                g2.fillRoundRect(barX, barY, fill, barH, 4, 4);

                g2.setColor(Theme.textSecondary());
                String value = String.valueOf(entry.getValue());
                g2.drawString(value, getWidth() - valueWidth, y + rowH / 2 + fm.getAscent() / 2 - 3);
                y += rowH;
            }
            g2.dispose();
        }

        private String truncate(Graphics2D g2, String text, int maxWidth) {
            FontMetrics fm = g2.getFontMetrics();
            if (fm.stringWidth(text) <= maxWidth) {
                return text;
            }
            String ellipsis = "…";
            while (!text.isEmpty() && fm.stringWidth(text + ellipsis) > maxWidth) {
                text = text.substring(0, text.length() - 1);
            }
            return text + ellipsis;
        }
    }

    /** Vertical bar chart for a time series. */
    private static final class VerticalBars extends JPanel {

        private final Map<String, Long> data;

        private VerticalBars(Map<String, Long> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (data == null || data.isEmpty()) {
                g2.setColor(Theme.textMuted());
                g2.setFont(Theme.caption());
                g2.drawString("No circulation recorded yet", 4, 20);
                g2.dispose();
                return;
            }

            int bottom = 22;
            int top = 16;
            int chartH = Math.max(20, getHeight() - bottom - top);
            int chartW = getWidth() - 8;
            long max = data.values().stream().mapToLong(v -> v != null ? v : 0L).max().orElse(1);

            g2.setColor(Theme.border());
            g2.drawLine(4, getHeight() - bottom, getWidth() - 4, getHeight() - bottom);

            int slot = chartW / data.size();
            int barW = Math.max(8, Math.min(34, slot - 12));
            int x = 4 + (slot - barW) / 2;

            for (Map.Entry<String, Long> entry : data.entrySet()) {
                int h = (int) (chartH * (entry.getValue() / (double) max));
                g2.setColor(Theme.accent());
                g2.fillRoundRect(x, getHeight() - bottom - h, barW, Math.max(2, h), 4, 4);

                g2.setFont(Theme.caption());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Theme.textSecondary());
                String value = String.valueOf(entry.getValue());
                if (entry.getValue() > 0) {
                    g2.drawString(value, x + (barW - fm.stringWidth(value)) / 2,
                            getHeight() - bottom - h - 4);
                }
                g2.setColor(Theme.textMuted());
                String label = entry.getKey() == null ? "" : entry.getKey();
                g2.drawString(label, x + (barW - fm.stringWidth(label)) / 2, getHeight() - bottom + 14);
                x += slot;
            }
            g2.dispose();
        }
    }
}
