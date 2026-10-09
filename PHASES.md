# LibraX — Implementation Phases

Work proceeds strictly phase-by-phase per the workflow in `RULES.md`. Before
starting a phase: read `README.md`, `ARCHITECTURE.md`, `RULES.md`,
`MEMORY.md`, inspect existing source, then implement **only** that phase's
scope, run `mvn clean test`, fix issues, and update `MEMORY.md`.

---

## Phase 1 — Project Initialization ✅ (this delivery)

**Objective:** Establish a buildable Maven skeleton with correct package
structure, configuration loading, and logging — no business features yet.

**Tasks:**
- Create Maven project (`pom.xml`) with pinned dependencies for Java 21.
- Create full package structure under `com.librax`.
- Implement `App.java` entry point (sets FlatLaf L&F, loads config, boots
  logging, launches `LoginFrame` placeholder).
- Implement `config.AppConfig`, `config.DatabaseConfig`, `config.ApiConfig`.
- Implement base exception hierarchy (`LibraXException` + subtypes).
- Configure Logback (`logback.xml`) and `application.properties`.
- Add `.gitignore`.
- Add all top-level documentation files.

**Files/classes:** `pom.xml`, `App.java`, `config/*`, `exception/*`,
`logback.xml`, `application.properties`, `.gitignore`, all `*.md` docs.

**Dependencies:** none (first phase).

**Acceptance criteria:**
- `mvn clean package` produces `target/librax.jar`.
- `java -jar target/librax.jar` starts, sets FlatLaf theme, logs startup,
  and shows a placeholder window (real `LoginFrame` arrives in Phase 5/6).
- No business logic present yet — only scaffolding.

**Testing requirements:** a smoke test verifying `AppConfig` loads default
properties correctly.

---

## Phase 2 — Database & Entities

**Objective:** Define the normalized SQLite schema and domain `model`
classes/records.

**Tasks:** schema DDL (`db/schema.sql`), `DatabaseManager` (connection +
foreign-key pragma + schema execution), all `model` classes/records/enums
(`User`, `Role`, `Member`, `Author`, `Category`, `Book`, `BookCopy`, `Loan`,
`Reservation`, `Fine`, `Payment`, `AuditLog`, `LibrarySettings`).

**Files/classes:** `config/DatabaseConfig`, a `DatabaseManager` in
`repository.impl` (or `config`), `model/*`.

**Dependencies:** Phase 1.

**Acceptance criteria:** schema creates cleanly on a fresh SQLite file;
`PRAGMA foreign_keys` is `ON`; model classes compile and are immutable
where sensible (records for value-like entities).

**Testing requirements:** integration test that boots a temp database and
asserts all tables/indexes exist.

---

## Phase 3 — Repository / DAO Layer

**Objective:** JDBC repositories implementing repository interfaces, using
`PreparedStatement` and try-with-resources exclusively.

**Tasks:** `UserRepository`/`JdbcUserRepository`, `BookRepository`/
`JdbcBookRepository`, `MemberRepository`/`JdbcMemberRepository`,
`LoanRepository`, `ReservationRepository`, `FineRepository` (+ impls).

**Dependencies:** Phase 2.

**Acceptance criteria:** CRUD + query methods for each aggregate; no SQL
outside `repository.impl`.

**Testing requirements:** integration tests per repository against a temp
SQLite DB (insert/read/update/delete round-trips).

---

## Phase 4 — Service Layer

**Objective:** Business rules on top of repositories, exposed to UI.

**Tasks:** `AuthenticationService`, `BookService`, `MemberService`,
`LoanService`, `ReservationService`, `FineService` (+ Strategy interface),
`ReportService`, `AnalyticsService`.

**Dependencies:** Phase 3.

**Acceptance criteria:** all validation rules from the spec (empty title,
invalid ISBN/email, duplicate ISBN, expired membership, zero copies,
nonexistent loan, invalid payment) are enforced here, not in UI.

**Testing requirements:** unit tests with Mockito-mocked repositories for
each service's core rules.

---

## Phase 5 — Authentication / Security

**Objective:** Secure login, sessions, first-run admin creation.

**Tasks:** `security.PasswordHasher` (BCrypt), `security.SessionManager`,
first-run admin initialization flow, failed-login handling.

**Dependencies:** Phase 4.

**Acceptance criteria:** no plaintext passwords anywhere; first run forces
creation of an admin account (no shipped default password); session logout
clears state.

**Testing requirements:** unit tests for `AuthenticationService` success/
failure/lockout paths.

---

## Phase 6 — Modern Swing UI (shell)

**Objective:** `LoginFrame`, `MainFrame`, `Sidebar`, `Header`, and reusable
`ui.components`, styled with FlatLaf.

**Dependencies:** Phase 5.

**Acceptance criteria:** navigable shell with placeholder panels for every
nav item; responsive to window resize; consistent theme.

**Testing requirements:** manual UI verification checklist (Swing UI is not
unit-tested directly); component-level tests where logic is extractable.

---

## Phase 7 — Book Management

**Objective:** Full `BookPanel` wired to `BookService`.

**Dependencies:** Phase 6.

**Acceptance criteria:** add/edit/delete/search/filter/sort/view-details/
copy management all functional against SQLite.

---

## Phase 8 — Member Management

**Objective:** Full `MemberPanel` wired to `MemberService`.

**Dependencies:** Phase 6.

**Acceptance criteria:** CRUD + validation + borrowing history view.

---

## Phase 9 — Issue / Return

**Objective:** `LoanPanel` implementing the 10-step issue flow and 8-step
return flow from the spec.

**Dependencies:** Phases 7, 8.

**Acceptance criteria:** availability, membership status, borrowing limit,
and blocking-fine checks all enforced; audit logs written.

---

## Phase 10 — Reservations and Fines

**Objective:** `ReservationPanel`, `FinePanel`, payment recording.

**Dependencies:** Phase 9.

**Acceptance criteria:** reservation state machine works end-to-end;
overdue fines auto-calculate on return; manual adjustment audited; payments
recorded with method (`CASH`/`UPI`/`CARD`/`OTHER`).

---

## Phase 11 — Google Books API

**Objective:** `GoogleBooksClient`, `GoogleBooksService`, search UI, import
workflow, full failure handling, background execution via `SwingWorker`.

**Dependencies:** Phase 7 (books), Phase 6 (UI shell).

**Acceptance criteria:** app remains fully functional with no internet;
all failure modes from the spec produce friendly errors, not crashes.

---

## Phase 12 — Dashboard and Analytics

**Objective:** `DashboardPanel` KPIs + JFreeChart visualizations backed by
`AnalyticsService` queries.

**Dependencies:** Phases 7–10.

**Acceptance criteria:** all dashboard numbers/charts come from live
database data, not mock data.

---

## Phase 13 — Reports / Export

**Objective:** `ReportPanel`, `ReportService` CSV export (PDF if practical).

**Dependencies:** Phase 12.

**Acceptance criteria:** every report type in the spec exports correctly;
generation for large datasets runs off the EDT.

---

## Phase 14 — Testing (hardening pass)

**Objective:** Close coverage gaps across services, validation, and
`GoogleBooksService`; add remaining integration tests.

**Dependencies:** all functional phases.

**Acceptance criteria:** `mvn clean test` green; meaningful coverage of
business rules and edge cases, not artificial 100%.

---

## Phase 15 — Security Review

**Objective:** Audit for plaintext secrets, SQL injection surfaces, missing
authorization checks, unsafe error messages.

**Dependencies:** all functional phases.

**Acceptance criteria:** checklist in `RULES.md` §10 fully satisfied.

---

## Phase 16 — Documentation

**Objective:** Bring all `.md` docs fully in sync with final implementation.

**Dependencies:** all prior phases.

**Acceptance criteria:** README/ARCHITECTURE/DATABASE/API/TESTING/DESIGN all
accurate; `MEMORY.md` reflects final state.

---

## Phase 17 — Packaging and Final Demo

**Objective:** Final `mvn clean package`, executable jar, end-to-end demo
verification against the Quality Gate checklist in the project brief.

**Dependencies:** all prior phases.

**Acceptance criteria:** every item in the Quality Gate checklist passes.
