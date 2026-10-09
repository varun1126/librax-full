# LibraX — UI/UX Design Specification

LibraX must look and feel like a modern professional desktop application,
not a default Swing/AWT app. This is achieved with FlatLaf plus disciplined,
consistent component styling.

## 1. Color System

Defined as FlatLaf UI defaults / client properties so both light and dark
themes derive from the same semantic tokens.

| Token | Light | Dark | Usage |
|---|---|---|---|
| `color.primary` | `#2563EB` | `#3B82F6` | Primary buttons, active nav item, links |
| `color.primary.hover` | `#1D4ED8` | `#60A5FA` | Hover state |
| `color.surface` | `#FFFFFF` | `#1E1E2E` | Cards, panels |
| `color.background` | `#F4F6FB` | `#141420` | Window background |
| `color.border` | `#E2E8F0` | `#2A2A3C` | Dividers, card borders |
| `color.text.primary` | `#1E293B` | `#E2E8F0` | Main text |
| `color.text.secondary` | `#64748B` | `#94A3B8` | Secondary/help text |
| `color.success` | `#16A34A` | `#22C55E` | Available, paid, success badges |
| `color.warning` | `#D97706` | `#F59E0B` | Due-soon, waiting |
| `color.danger` | `#DC2626` | `#EF4444` | Overdue, errors, destructive actions |
| `color.info` | `#0891B2` | `#22D3EE` | Informational badges |

## 2. Typography

- Primary font: **Inter** (fallback: `Segoe UI` / system default via
  FlatLaf if not bundled).
- Scale: `H1` 22sp semibold (page titles), `H2` 16sp semibold (card/section
  titles), `Body` 13sp regular, `Caption` 11.5sp regular (secondary text),
  `Badge` 11sp semibold uppercase.
- Line height ~1.4x font size for readability in tables and forms.

## 3. Spacing

8px base unit. Standard paddings: card `16px`, form field vertical gap
`12px`, section gap `24px`, sidebar item padding `10px 16px`.

## 4. Buttons

- **Primary:** filled `color.primary`, white text, 6px corner radius.
- **Secondary:** outlined, `color.border`, text `color.text.primary`.
- **Destructive:** filled `color.danger`, used for delete/cancel actions,
  always behind a confirmation dialog.
- **Icon buttons:** used in table rows (edit/delete/view) — 32x32 hit area
  minimum for usability.
- Disabled state: 40% opacity, no hover feedback.

## 5. Cards

Used for dashboard KPIs and grouped forms: `color.surface` background,
1px `color.border`, 10px corner radius, subtle shadow (FlatLaf
`Shadow` support or a custom `RoundedPanel`), 16px internal padding.

## 6. Tables

- Modern FlatLaf-styled `JTable`: no cell borders, alternating row tint
  (`color.background` on odd rows), 36px row height, sortable headers.
- Status columns render as **badges** (see §11), not plain text.
- Right-aligned numeric columns (copies, fines).
- Toolbar above every table: search box, filter dropdowns, primary action
  button (e.g. "Add Book") right-aligned.

## 7. Dialogs

- Centered over `MainFrame`, 8px corner radius, header with title + close.
- Confirmation dialogs use the destructive-button color for the confirming
  action when the action is destructive, and clear, specific copy
  ("Delete 'Clean Code' by Robert C. Martin?"), never a bare "Are you sure?".

## 8. Forms

- Label above field (not beside), consistent field height (32px), inline
  validation messages in `color.danger` directly under the offending field.
- Required fields marked with a subtle asterisk, not color alone.
- Primary submit button bottom-right; cancel/secondary bottom-left or
  adjacent, clearly de-emphasized.

## 9. Sidebar

Fixed-width (220px) vertical navigation: Dashboard, Books, Members,
Issue/Return, Reservations, Fines, Reports, Analytics, Audit Logs,
Settings. Active item highlighted with `color.primary` background tint and
a left accent bar. Icons (via FlatLaf SVG icon support) precede each label.
Collapsible to icon-only width for smaller windows.

## 10. Header

Top bar: current page title (left), global search (center, books/members),
theme toggle + logged-in user menu with role badge + logout (right).

## 11. Status Badges

Small pill-shaped labels, uppercase, 11sp semibold, colored by semantic
state:

| State | Color |
|---|---|
| AVAILABLE / PAID / ACTIVE | `color.success` |
| WAITING / DUE SOON | `color.warning` |
| OVERDUE / UNPAID / EXPIRED | `color.danger` |
| ISSUED / READY | `color.info` |

## 12. Notifications

Non-blocking toast-style notifications (top-right, auto-dismiss ~4s) for
successful actions ("Book issued to Priya Sharma"). Blocking dialogs are
reserved for confirmations and errors that require acknowledgement.

## 13. Empty States

Every list/table has a designed empty state: centered icon, one-line
message ("No overdue books 🎉" / "No books match your search"), and, where
relevant, a primary action ("Add your first book").

## 14. Loading States

- Table/panel-level: skeleton rows or a centered `JProgressBar`
  (indeterminate) with a short label ("Loading books…").
- Google Books search: an inline spinner in the results area while the
  `SwingWorker` runs; the search button is disabled mid-request.

## 15. Error States

- Inline for validation (field-level).
- Toast/banner for recoverable failures (e.g. Google Books unreachable):
  friendly message + optional "Retry" action.
- Full dialog only for actions that cannot proceed at all.

## 16. Dark / Light Theme Behavior

- FlatLaf `FlatLightLaf` / `FlatDarkLaf` (or IntelliJ-derived themes)
  selected via `Settings` and persisted in `library_settings`.
- All custom components read colors from FlatLaf `UIManager` keys /
  client properties — never hardcoded `Color` literals — so theme
  switching updates the entire UI without restart.
- Charts (JFreeChart) re-theme their background/gridline/text colors to
  match the active Laf.
