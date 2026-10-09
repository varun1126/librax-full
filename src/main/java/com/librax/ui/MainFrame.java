package com.librax.ui;

import com.librax.AppContext;
import com.librax.model.User;
import com.librax.ui.books.BookPanel;
import com.librax.ui.dashboard.DashboardPanel;
import com.librax.ui.fines.FinePanel;
import com.librax.ui.loans.LoanPanel;
import com.librax.ui.members.MemberPanel;
import com.librax.ui.reports.ReportPanel;
import com.librax.ui.reservations.ReservationPanel;
import com.librax.ui.settings.SettingsPanel;
import com.librax.ui.shell.SidebarPanel;
import com.librax.ui.shell.TopBar;
import com.librax.ui.theme.AppTheme;
import com.librax.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Application shell: dark sidebar navigation, workspace top bar and a
 * card-layout content area. Sections are created lazily on first
 * navigation and refreshed when they become visible.
 */
public class MainFrame extends JFrame {

    private final AppContext ctx;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<Section, JComponent> panels = new EnumMap<>(Section.class);
    private final SidebarPanel sidebar;
    private final TopBar topBar;

    /**
     * Builds and shows the shell.
     *
     * @param ctx composition root providing services and configuration
     */
    public MainFrame(AppContext ctx) {
        super("LibraX — " + ctx.appConfig.getLibraryName());
        this.ctx = ctx;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        User user = ctx.sessionManager.getCurrentUser();
        String name = user == null ? "" : (isBlank(user.getFullName()) ? user.getUsername() : user.getFullName());
        String role = user == null ? "" : String.valueOf(user.getRole());

        topBar = new TopBar(this::searchBooks, this::toggleTheme);
        topBar.applyThemeState(Theme.isDark());
        sidebar = new SidebarPanel(this::navigate, this::signOut, name, role);

        content.setOpaque(true);
        content.setBackground(Theme.workspace());

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(true);
        center.setBackground(Theme.workspace());
        center.add(topBar, BorderLayout.NORTH);
        center.add(content, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);

        navigate(Section.DASHBOARD);
    }

    // ------------------------------------------------------------- navigation

    /**
     * Shows a section, creating it on first use.
     *
     * @param section destination section
     */
    public void navigate(Section section) {
        ensurePanel(section);
        cardLayout.show(content, section.name());
        topBar.setSection(section);
        sidebar.setActive(section);
        if (section == Section.DASHBOARD) {
            ((DashboardPanel) panels.get(section)).refresh();
        }
    }

    private void ensurePanel(Section section) {
        if (panels.containsKey(section)) {
            return;
        }
        JComponent panel = switch (section) {
            case DASHBOARD -> new DashboardPanel(ctx);
            case BOOKS -> new BookPanel(ctx);
            case MEMBERS -> new MemberPanel(ctx);
            case LOANS -> new LoanPanel(ctx);
            case RESERVATIONS -> new ReservationPanel(ctx);
            case FINES -> new FinePanel(ctx);
            case REPORTS -> new ReportPanel(ctx);
            case SETTINGS -> new SettingsPanel(ctx);
        };

        if (section.scrollable) {
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Theme.workspace());
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            content.add(scroll, section.name());
        } else {
            content.add(panel, section.name());
        }
        panels.put(section, panel);
    }

    // ---------------------------------------------------------------- actions

    /** Runs the global search box against the books catalog. */
    private void searchBooks(String query) {
        if (isBlank(query)) {
            return;
        }
        ensurePanel(Section.BOOKS);
        ((BookPanel) panels.get(Section.BOOKS)).applySearch(query.trim());
        navigate(Section.BOOKS);
    }

    /**
     * Switches palette by re-installing the theme and rebuilding the shell,
     * so every component is constructed against the new tokens.
     */
    private void toggleTheme() {
        switchTheme(ctx, this);
    }

    /**
     * Applies the opposite palette and rebuilds the shell in place.
     *
     * @param ctx     composition root used to construct the new shell
     * @param current window being replaced; may be {@code null}
     */
    public static void switchTheme(AppContext ctx, java.awt.Window current) {
        AppTheme.setDark(!Theme.isDark());
        rebuild(ctx, current);
    }

    /**
     * Recreates the shell against the currently installed tokens, keeping
     * the previous window position.
     *
     * @param ctx     composition root used to construct the new shell
     * @param current window being replaced; may be {@code null}
     */
    public static void rebuild(AppContext ctx, java.awt.Window current) {
        int x = current == null ? 0 : current.getX();
        int y = current == null ? 0 : current.getY();
        if (current != null) {
            current.dispose();
        }
        MainFrame next = new MainFrame(ctx);
        next.setLocation(x, y);
        next.setVisible(true);
    }

    private void signOut() {
        ctx.authenticationService.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(ctx).setVisible(true));
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    /** Closes the shell without exiting the JVM (used by tests/tools). */
    public void requestClose() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}
