# LibraX — Project Memory

> Updated after every major phase. This is the single source of truth for
> "where the project currently stands" — read this before starting any
> new phase.

## Project Purpose

LibraX is a university-grade (≈80 mark) desktop Library Management System
in Java 21, demonstrating layered architecture, JDBC/SQLite, Swing/FlatLaf
UI, Google Books API integration, security, concurrency, testing, and
documentation to a professional standard.

## Current Architecture

Layered: `ui → service → repository → SQLite`, with a parallel
`ui → GoogleBooksService → GoogleBooksClient → Google Books API` chain for
external integration. See `ARCHITECTURE.md` for full detail (data-flow
diagrams for auth, issue, return, reservation, fine calculation, Google
Books).

## Completed Phases

> **Note on process:** phases 2–13 were compressed into a single rapid
> delivery (at the user's request for a working app "by tomorrow")
> instead of being built and quality-gated one at a time as
> `RULES.md`/`PHASES.md` describe. Treat everything below as **written
> but not yet verified by `mvn clean test`** — this sandbox has neither
> `mvn` nor `javac` available, so nothing here has been compiled. This is
> the top priority to fix before trusting the build.

- **Phase 1 — Project Initialization**: Maven project (`pom.xml`, pinned
  dependency versions), full `com.librax` package skeleton, `AppConfig`/
  `DatabaseConfig`/`ApiConfig`, exception hierarchy, Logback config,
  `.gitignore`, all 10 docs, `AppConfigTest` smoke test.

- **Phase 2 — Database & Entities**: `src/main/resources/db/schema.sql`
  (all 13 tables, FKs, CHECK constraints, indexes, default roles seeded
  via `INSERT OR IGNORE`). `DatabaseManager` opens SQLite connections
  with `PRAGMA foreign_keys = ON` and idempotently applies the schema on
  startup. All `model` classes/enums implemented as plain mutable POJOs
  (not records, to keep JDBC row-mapping straightforward for a fast
  build — could be revisited).

- **Phase 3 — Repository/DAO Layer**: `Jdbc*Repository` implementations
  for User, Member, Book (+ copies, authors, categories), Loan,
  Reservation, Fine (+ Payment), AuditLog. `PreparedStatement` +
  try-with-resources throughout; no string-concatenated SQL anywhere.

- **Phase 4 — Service Layer**: `AuthenticationService`, `BookService`,
  `MemberService`, `LoanService`, `ReservationService`, `FineService`
  (+ `FineCalculationStrategy`/`DailyFlatRateStrategy`), `ReportService`,
  `AnalyticsService`. Validation rules from the brief are enforced here.

- **Phase 5 — Authentication/Security**: `PasswordHasher` (BCrypt, cost
  12), `SessionManager`, first-run admin creation flow in
  `AuthenticationService`/`LoginFrame`, failed-login lockout after 5
  attempts.

- **Phase 6 — Swing UI shell**: `LoginFrame` (login + first-run admin
  setup), `MainFrame` (sidebar + header + `CardLayout` content),
  `StatusBadge`/`RoundedPanel`/`DashboardCard` reusable components.
  FlatLaf applied via `App.java`.

- **Phases 7–10 — Book/Member/Loan/Reservation/Fine management**:
  `BookPanel` (+ `BookFormDialog`), `MemberPanel`, `LoanPanel` (issue by
  member/book ID + return), `ReservationPanel`, `FinePanel` (+ payment
  recording) all wired directly to their services.

- **Phase 11 — Google Books API**: `GoogleBooksClient` (java.net.http,
  Jackson mapping, HTTP 429/timeout/connect-failure handling all mapped
  to user-safe `ApiException` messages), `GoogleBooksService` (query
  building per search type, DTO flattening), `GoogleBooksSearchDialog`
  running the call on a `SwingWorker` off the EDT, with an
  **"Import to Library"** action that maps into `BookService.createBook`.

- **Phase 12 — Dashboard/Analytics**: `AnalyticsService` aggregate
  queries backing 8 KPI cards + 2 charts. **Simplification:** charts are
  a small hand-rolled `SimpleBarChart` (custom `paintComponent`), not
  JFreeChart yet, to save time — `jfreechart` is still a pinned pom.xml
  dependency and swapping the chart panel implementation is a contained,
  isolated follow-up (`DashboardPanel.SimpleBarChart`).

- **Phase 13 — Reports/Export**: `ReportService` CSV export (books,
  overdue, members, fine collection, per-member borrowing history) run
  via `SwingWorker` from `ReportPanel`. PDF export not yet implemented.

## Pending / Not Yet Done

- **Compilation has not been verified anywhere in this delivery** — run
  `mvn clean test` immediately and report any errors.
- Phase 14 (deeper test coverage — only `AppConfigTest` currently exists;
  `AuthenticationService`/`BookService`/`LoanService`/`FineService`/
  `ValidationUtils`/`GoogleBooksService` unit tests from `TESTING.md` are
  not yet written).
- Phase 15 (formal security review pass).
- Audit Logs UI panel and Analytics-as-its-own-nav-item are not wired
  into `MainFrame`'s sidebar yet (audit data is captured correctly via
  `AuditLogger`/`audit_logs`, just not browsable in the UI yet).
- PDF report export.
- JFreeChart swap-in for the dashboard (see Phase 12 note above).
- `LoanPanel`/`ReservationPanel` currently ask for member/book **IDs**
  typed by hand rather than a proper picker — functional but not
  polished; a searchable combo/autocomplete would be the natural next
  step.
- Full Javadoc coverage is present on services/config/security but is
  thinner on the `ui` package, to save time under the deadline.

## Important Technical Decisions

- **SQLite** chosen for a zero-install, fully offline-capable desktop app.
- **BCrypt** (`at.favre.lib:bcrypt`) over hand-rolled hashing for password
  storage.
- **Maven Shade plugin** produces a single runnable `librax.jar` for easy
  grading/demo (`mvn clean package` → `java -jar target/librax.jar`).
- **Constructor dependency injection** everywhere (no framework, no field
  injection) to keep the service layer trivially mockable with Mockito.
- **Strategy pattern** reserved for fine calculation so the algorithm can
  change without touching `LoanService` or UI code.

## Database Decisions

- Dates/timestamps stored as ISO-8601 `TEXT` (SQLite has no native
  temporal type); parsed via `java.time` at the repository boundary.
- `books.available_copies` is a maintained cached value (updated
  transactionally alongside `book_copies.status`), not recomputed via
  `COUNT` on every read, for dashboard performance.
- Foreign-key enforcement (`PRAGMA foreign_keys = ON`) is set on every
  JDBC connection opened by `DatabaseManager` (arrives Phase 2).

## API Decisions

- Google Books integration isolated behind `GoogleBooksClient` (raw HTTP +
  JSON) and `GoogleBooksService` (mapping + orchestration); repository
  code never sees Google DTOs, and the `api` package never sees domain
  `model` types directly (mapping happens in `service`).
- All Google Books calls run off the Swing EDT via `SwingWorker`.
- One lightweight retry for transient network failures only; 4xx and JSON
  errors are not retried.

## UI Decisions

- FlatLaf for modern theming (light/dark), MigLayout for flexible form
  layout, reusable components centralized in `ui.components`.
- Status shown via colored badge components, not plain text, per
  `DESIGN.md` §11.

## Known Issues

- None yet — only scaffolding exists (Phase 1). No functional code paths
  are implemented besides configuration loading and application bootstrap.
- **Sandbox note:** this development environment's network egress does
  not include Maven Central, so `mvn clean test`/`mvn clean package`
  could not be executed here to verify the build. The `pom.xml` and
  source were written carefully against known-compatible, real released
  versions of each dependency, but **run `mvn clean test` in an
  environment with normal internet access before trusting the build.**

## Future Improvements

- PDF report generation (currently CSV-only planned baseline).
- Notifications for reservation readiness / due-date reminders.
- Multi-branch library support.
- Barcode/RFID scanning for `book_copies`.
