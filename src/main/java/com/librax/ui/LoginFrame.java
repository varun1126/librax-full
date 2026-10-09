package com.librax.ui;

import com.librax.AppContext;
import com.librax.exception.AuthenticationException;
import com.librax.model.User;
import com.librax.ui.components.CardPanel;
import com.librax.ui.components.FormBuilder;
import com.librax.ui.components.Ui;
import com.librax.ui.theme.Theme;
import com.librax.util.DialogUtils;
import com.librax.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Sign-in screen: a single restrained card on the workspace background
 * with the brand mark, credentials form and inline error feedback.
 *
 * <p>When no users exist yet the same shell presents first-run
 * administrator creation instead of a login form (README.md §14).</p>
 */
public class LoginFrame extends JFrame {

    private final AppContext ctx;
    private final JLabel errorLabel = new JLabel(" ");

    /**
     * Builds the frame in login or first-run mode.
     *
     * @param ctx composition root providing services and configuration
     */
    public LoginFrame(AppContext ctx) {
        super("LibraX — Sign In");
        this.ctx = ctx;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JComponent form = ctx.authenticationService.isFirstRun() ? buildFirstRunForm() : buildLoginForm();
        setContentPane(buildContent(form));
        pack();
        if (getWidth() < 440) {
            setSize(440, getHeight());
        }
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ shell

    private JPanel buildContent(JComponent form) {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.workspace());
        workspace.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        CardPanel card = new CardPanel(new BorderLayout());
        JPanel inner = new JPanel(new BorderLayout(0, 14));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));
        inner.add(buildBrand(), BorderLayout.NORTH);
        inner.add(form, BorderLayout.CENTER);
        inner.add(footer(), BorderLayout.SOUTH);
        card.add(inner, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(412, card.getPreferredSize().height));

        workspace.add(card, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel buildBrand() {
        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.add(new BrandMark(), BorderLayout.WEST);

        JPanel titles = new JPanel(new BorderLayout(0, 1));
        titles.setOpaque(false);
        JLabel name = new JLabel(ctx.appConfig.getLibraryName());
        name.setFont(Theme.body().deriveFont(Font.BOLD, 14f));
        name.setForeground(Theme.textPrimary());
        JLabel subtitle = new JLabel("Library Management System");
        subtitle.setFont(Theme.caption());
        subtitle.setForeground(Theme.textSecondary());
        titles.add(name, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);
        brand.add(titles, BorderLayout.CENTER);
        return brand;
    }

    private JLabel footer() {
        JLabel footer = new JLabel("LibraX desktop client · v1.0.0", JLabel.CENTER);
        footer.setFont(Theme.caption());
        footer.setForeground(Theme.textMuted());
        return footer;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(message != null && !message.isBlank());
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------ login

    private JComponent buildLoginForm() {
        JPanel form = new JPanel(new MigLayout("fillx, ins 0, gap 0", "[grow]"));
        form.setOpaque(false);

        JLabel heading = new JLabel("Sign in");
        heading.setFont(Theme.pageTitle());
        heading.setForeground(Theme.textPrimary());
        form.add(heading, "gapbottom 4, wrap");

        JLabel hint = Ui.caption("Use your staff account to continue.");
        form.add(hint, "gapbottom 14, wrap");

        FormBuilder fields = new FormBuilder(form);
        JTextField username = fields.textField("Username", null, true);
        JPasswordField password = fields.passwordField("Password", true);

        JButton signIn = Ui.primary("Sign in");
        signIn.setPreferredSize(new Dimension(signIn.getPreferredSize().width, Ui.CONTROL_HEIGHT + 3));
        form.add(signIn, "growx, gaptop 16");

        errorLabel.setFont(Theme.caption());
        errorLabel.setForeground(Theme.dangerText());
        errorLabel.setVisible(false);
        form.add(errorLabel, "gaptop 8, wrap");

        Runnable submit = () -> {
            try {
                ctx.authenticationService.authenticate(username.getText().trim(), password.getPassword());
                openMainFrame();
            } catch (AuthenticationException ex) {
                showError(ex.getMessage());
            }
        };
        signIn.addActionListener(e -> submit.run());
        password.addActionListener(e -> submit.run());
        return form;
    }

    // -------------------------------------------------------------- first run

    private JComponent buildFirstRunForm() {
        JPanel outer = new JPanel(new MigLayout("fillx, ins 0, gap 0", "[grow]"));
        outer.setOpaque(false);

        JLabel heading = new JLabel("Create the administrator account");
        heading.setFont(Theme.pageTitle());
        heading.setForeground(Theme.textPrimary());
        outer.add(heading, "gapbottom 4, wrap");

        JLabel hint = Ui.caption("This account manages users, policy and data for this library.");
        hint.setBorder(null);
        outer.add(hint, "gapbottom 14, wrap");

        FormBuilder fields = new FormBuilder(outer);
        JTextField fullName = fields.textField("Full name", null, true);
        JTextField email = fields.textField("Email", null, false);
        JTextField username = fields.textField("Username", null, true);
        JPasswordField password = fields.passwordField("Password", true);
        JPasswordField confirm = fields.passwordField("Confirm password", true);

        JButton create = Ui.primary("Create account and continue");
        create.setPreferredSize(new Dimension(create.getPreferredSize().width, Ui.CONTROL_HEIGHT + 3));
        outer.add(create, "growx, gaptop 16");

        errorLabel.setFont(Theme.caption());
        errorLabel.setForeground(Theme.dangerText());
        errorLabel.setVisible(false);
        outer.add(errorLabel, "gaptop 8, wrap");

        create.addActionListener(e -> {
            char[] pw1 = password.getPassword();
            char[] pw2 = confirm.getPassword();
            if (ValidationUtils.isBlank(username.getText()) || ValidationUtils.isBlank(fullName.getText())) {
                DialogUtils.showError(this, "Full name and username are required.");
                return;
            }
            if (pw1.length == 0) {
                DialogUtils.showError(this, "Choose a password.");
                return;
            }
            if (!new String(pw1).equals(new String(pw2))) {
                showError("Passwords do not match.");
                return;
            }
            try {
                User admin = ctx.authenticationService.createFirstAdministrator(
                        username.getText().trim(), pw1, fullName.getText().trim(), email.getText().trim());
                ctx.authenticationService.authenticate(admin.getUsername(), pw1);
                openMainFrame();
            } catch (AuthenticationException ex) {
                showError(ex.getMessage());
            }
        });
        return outer;
    }

    private void openMainFrame() {
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(ctx).setVisible(true));
    }

    /** Accent tile with the LX initials. */
    private static final class BrandMark extends JComponent {
        private static final int SIZE = 30;

        private BrandMark() {
            setPreferredSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.accent());
            g2.fillRoundRect(0, 0, SIZE, SIZE, 7, 7);
            g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            String text = "LX";
            g2.drawString(text, (SIZE - fm.stringWidth(text)) / 2,
                    (SIZE - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
