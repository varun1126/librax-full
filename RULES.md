# LibraX — Development Rules

These rules apply to every phase of implementation. Violating them is a
defect, not a style preference.

## 1. Language & Platform

- Java 21 only. No deprecated APIs. No preview features relied upon for
  core functionality.
- Build exclusively through Maven (`pom.xml` is the single source of truth
  for dependencies/versions).

## 2. Clean Code & SOLID

- Single Responsibility: a class does one job (a `Panel` displays; a
  `Service` decides; a `Repository` persists).
- Open/Closed: prefer adding a new `FineCalculationStrategy` over branching
  on type inside `FineService`.
- Liskov/Interface Segregation: repository interfaces expose only the
  operations their consumers need.
- Dependency Inversion: services depend on repository **interfaces**, not
  `Jdbc*` implementations; wired via constructor injection.

## 3. Layering Rules (non-negotiable)

- **No SQL in UI.** Ever.
- **No business logic in UI.** Panels call `service` methods and render
  results; they do not decide loan periods, fine amounts, or eligibility.
- **No database entities exposed to external API code.** The `api` package
  only ever sees `dto` types; mapping to `model` happens in `service`.
- **PreparedStatement only.** No string concatenation of user input into
  SQL, ever, for any query.

## 4. Dependency Injection

- Constructor injection everywhere. No field injection, no service locators,
  no static mutable singletons except the narrowly justified
  `DatabaseManager` connection provider and `AppConfig`.
- This is required for Mockito-based unit testing of the service layer.

## 5. Naming

- Classes: `UpperCamelCase` nouns (`LoanService`, `BookCopy`).
- Methods: `lowerCamelCase` verbs (`issueBook`, `calculateFine`).
- Constants: `UPPER_SNAKE_CASE` in a dedicated `config`/constants holder,
  never scattered as magic numbers/strings in business logic.
- Packages: all lowercase, matching the structure in ARCHITECTURE.md.

## 6. No Duplicate Code

- Shared Swing behavior goes in `ui.components` (e.g. `StatusBadge`,
  `SearchBar`, `ConfirmDialog`).
- Shared validation goes in `util.ValidationUtils`.
- If the same block appears twice, extract it before adding a third.

## 7. Exception Handling

- Never catch-and-ignore. Every `catch` either recovers meaningfully,
  rethrows as a typed `LibraXException` subtype, or logs and surfaces a
  user-friendly message.
- UI shows friendly text; logs contain the technical stack trace.
- Checked exceptions from JDBC are wrapped into unchecked
  `DatabaseException` at the repository boundary so service/UI code stays
  clean.

## 8. Logging

- SLF4J API only in code (`LoggerFactory.getLogger(...)`); Logback is the
  runtime binding, configured in `logback.xml`.
- Never log passwords, password hashes, or full session tokens.
- Log at appropriate levels: `INFO` for lifecycle/business events, `WARN`
  for recoverable issues, `ERROR` for failures, `DEBUG` for diagnostic
  detail.

## 9. Testing

- New service-layer logic ships with unit tests (JUnit 5 + Mockito) in the
  same phase it is written.
- Repository/database logic gets integration tests against a temporary
  SQLite file (or in-memory), not mocked JDBC internals.
- Tests must be deterministic — no reliance on real network calls; the
  Google Books client is mocked in `GoogleBooksService` tests.

## 10. Security

- BCrypt for all password storage.
- Role checks happen in the service layer, not just by hiding UI buttons.
- No secrets (API keys, credentials) committed to Git — see `.gitignore`
  and `API.md`.
- Error messages shown to users never leak stack traces, file paths, or
  SQL text.

## 11. Git Conventions

- Conventional, descriptive commit messages, e.g.:
  `feat(loans): add overdue fine calculation strategy`
  `fix(auth): correct BCrypt verification on empty password`
  `docs(architecture): add reservation state diagram`
- One phase (per `PHASES.md`) roughly corresponds to one logical group of
  commits.
- Never commit `target/`, IDE files, or local database files
  (see `.gitignore`).

## 12. Documentation Requirements

- Public classes and non-trivial public methods carry Javadoc explaining
  purpose, parameters, return values, and thrown exceptions.
- `MEMORY.md` is updated at the end of every phase (see workflow in
  `PHASES.md`).
- Architectural changes are reflected in `ARCHITECTURE.md`/`DATABASE.md`/
  `API.md` in the same phase that introduces them — documentation does not
  lag behind code.
