# LibraX — Testing Strategy

## 1. Strategy Overview

Testing follows the same layering as the codebase:

- **Unit tests** exercise the `service` layer in isolation, with
  repositories/clients replaced by Mockito mocks.
- **Integration tests** exercise the `repository` layer against a real,
  disposable SQLite database (a temp file per test class), to catch SQL
  and mapping bugs that mocks would hide.
- **UI** is validated primarily through a manual verification checklist
  (Swing UI is impractical to unit test meaningfully); any logic that can
  be extracted from a panel into a testable helper is extracted and
  tested.

## 2. Unit Tests (JUnit 5 + Mockito)

| Class under test | Key scenarios |
|---|---|
| `AuthenticationService` | correct password → success + session created; wrong password → failure + audit log; inactive user → rejected; failed-attempt counting |
| `BookService` | create with valid data; reject duplicate ISBN; reject empty title/invalid ISBN; update; delete; add/remove copies keeps `available_copies` consistent |
| `LoanService` | issue succeeds when eligible; rejects when no copies available; rejects when member expired/suspended; rejects when borrowing limit reached; rejects when blocking fines outstanding; return calculates overdue days correctly; return on-time produces zero fine |
| `FineService` | correct fine for N overdue days under the configured daily rate; zero fine when not overdue; manual adjustment recorded; payment reduces outstanding balance; over-payment rejected |
| `ValidationUtils` | valid/invalid email formats; valid/invalid ISBN-10 and ISBN-13 (checksum); valid/invalid phone; required-field checks |
| `GoogleBooksService` | maps a well-formed response correctly; handles empty `items`; wraps client `IOException`/timeout into `ApiException`; wraps malformed JSON into `ApiException`; handles HTTP 429 |

## 3. Integration Tests

| Repository | Scenarios |
|---|---|
| `JdbcBookRepository` | insert/read/update/delete round-trip; unique ISBN constraint violation surfaces as `DatabaseException`; search by title/author/category |
| `JdbcMemberRepository` | insert/read/update; duplicate membership number rejected |
| `JdbcLoanRepository` | create loan; find active loans by member; find overdue loans |
| `DatabaseManager` schema init | fresh DB creates all tables/indexes; foreign keys enforced (`PRAGMA foreign_keys`) |

Each integration test class creates an isolated temp SQLite file (JUnit 5
`@TempDir`) so tests never share state or depend on execution order.

## 4. UI Testing Approach

Manual checklist run at the end of relevant phases and again at Phase 17:

- Navigation between all sidebar sections works.
- Forms show inline validation errors for each invalid-input case in
  §20 of the project brief.
- Loading/empty/error states appear correctly (throttle/disable network
  to verify Google Books offline handling).
- Theme toggle (light/dark) updates all screens, including charts.
- Role-based UI: LIBRARIAN cannot see admin-only actions (manage
  librarians, security settings, delete audit logs).

## 5. Test Data

- `src/test/resources` holds fixture data (sample `GoogleVolumesResponseDto`
  JSON payloads for `GoogleBooksService` tests: well-formed, empty,
  malformed, missing-fields variants).
- Integration tests seed minimal rows directly via the repository under
  test (no shared "golden" dataset, to keep tests independent).

## 6. Important Edge Cases

- Issuing the last available copy of a book (copies hit zero, subsequent
  issue attempts fail cleanly).
- Returning a loan that was already returned.
- Returning a loan ID that does not exist.
- Reserving a book that is actually available (should this be blocked or
  short-circuited to "immediately available" — enforced by `ReservationService`).
- Fine payment amount exceeding the outstanding balance.
- Google Books search returning zero results vs. a network failure
  (must render two different, correct UI states).
- Expired membership attempting to issue a book.
- Concurrent return + reservation fulfillment ordering (first WAITING
  reservation becomes READY when a copy is returned).

## 7. How to Run Tests

```bash
mvn clean test
```

Runs the full JUnit 5 suite (unit + integration) via the Surefire plugin.
To run a single test class:

```bash
mvn -Dtest=LoanServiceTest test
```

## 8. Expected Results

- `mvn clean test` exits `0` with all tests passing before any phase is
  considered complete (Quality Gate, `RULES.md`/project brief §41).
- Failing tests are fixed in the same phase that introduced them — they
  are never carried forward or disabled to make the build pass.
