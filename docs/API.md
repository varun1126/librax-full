# LibraX — REST API Specification

## 1. Global Conventions

- **Base URL**: `/api` (or `http://localhost:8085/api`)
- **Authentication**: JWT Bearer token passed in the `Authorization` header:
  ```http
  Authorization: Bearer <token>
  ```
- **Response Format**: All endpoints return standard envelope responses:
  ```json
  {
    "success": true,
    "data": { ... },
    "message": "Operation completed successfully",
    "timestamp": "2026-09-27T17:15:00Z"
  }
  ```
- **Error Format**:
  ```json
  {
    "success": false,
    "errorCode": "VALIDATION_ERROR",
    "message": "Detailed user-friendly error message",
    "timestamp": "2026-09-27T17:15:00Z"
  }
  ```

---

## 2. API Endpoints Catalog

### System & Health

#### `GET /api/health`
- **Auth**: None (Public)
- **Description**: Returns operational status and database connectivity.
- **Response**:
  ```json
  {
    "success": true,
    "data": {
      "status": "UP",
      "application": "LibraX Intelligent Library System",
      "version": "1.0.0",
      "database": "UP"
    }
  }
  ```

---

### Authentication & Self-Registration

#### `POST /api/auth/login`
- **Auth**: None
- **Body**: `{ "identifier": "admin", "password": "Admin@123" }`
- **Description**: Staff & Administrator login. Rejects students.

#### `POST /api/auth/student-login`
- **Auth**: None
- **Body**: `{ "identifier": "student01", "password": "Student@123" }`
- **Description**: Student Portal login via username, email, or student barcode ID (`LIB-STU-XXXX`).

#### `POST /api/auth/register` (or `/student-register`)
- **Auth**: None
- **Body**:
  ```json
  {
    "fullName": "Priya Sharma",
    "email": "priya@college.edu",
    "password": "Password@123",
    "department": "Computer Science & Engineering",
    "courseName": "B.Tech Computer Science"
  }
  ```
- **Description**: Self-registration for new students. Generates member card and returns session token.

#### `GET /api/auth/me`
- **Auth**: Required
- **Description**: Returns the active session profile and student details.

---

### Books & Google Books API

#### `GET /api/books`
- **Auth**: None / Optional
- **Query Params**: `search`, `categoryId`, `availableOnly`, `page`, `limit`
- **Description**: Paginated catalog search across title, author, category, publisher, and ISBN.

#### `GET /api/books/{id}`
- **Auth**: None / Optional
- **Description**: Retrieves detailed book information and all physical copy barcodes.

#### `POST /api/books`
- **Auth**: `RequiresPermission("BOOK_CREATE")`
- **Description**: Manually register a book title and create physical barcodes.

#### `GET /api/books/google/search`
- **Auth**: `RequiresPermission("BOOK_IMPORT")`
- **Query Params**: `query=Clean+Code`
- **Description**: Queries live Google Books Volumes API for matching titles, authors, and cover art.

#### `POST /api/books/google/import`
- **Auth**: `RequiresPermission("BOOK_IMPORT")`
- **Body**: `GoogleBookDto` object with title, author, ISBN, and thumbnail.
- **Description**: Directly imports a book from Google Books into the college catalog.

---

### Circulation & Loans

#### `GET /api/loans`
- **Auth**: `RequiresPermission("LOAN_VIEW")`
- **Description**: If caller is a student, returns only their loans. If librarian/admin, returns all loans with pagination.

#### `POST /api/loans/issue`
- **Auth**: `RequiresPermission("LOAN_CREATE")`
- **Body**: `{ "memberId": 1, "bookId": 12 }`
- **Description**: Issues an available book copy to a student.

#### `POST /api/loans/{id}/return`
- **Auth**: `RequiresPermission("LOAN_RETURN")`
- **Description**: Returns book, checks for overdue fines, updates availability, and notifies next reservation holder.

#### `POST /api/loans/{id}/renew`
- **Auth**: `RequiresPermission("LOAN_RENEW")`
- **Description**: Extends loan due date by configured loan period (default: 14 days). IDOR protected.

---

### Fines & Cashiering (₹ INR)

#### `GET /api/fines`
- **Auth**: `RequiresPermission("FINE_VIEW")`
- **Description**: Returns fine assessments. Scoped to calling student if logged in as student.

#### `POST /api/fines/pay`
- **Auth**: `RequiresPermission("FINE_PAYMENT")`
- **Body**: `{ "fineId": 5, "amount": 10.00, "method": "UPI" }`
- **Description**: Records settlement via CASH, CARD, UPI, or ONLINE and generates a transaction receipt.

#### `POST /api/fines/{id}/waive`
- **Auth**: `RequiresPermission("FINE_WAIVE")`
- **Description**: Forgives outstanding balance with mandatory administrative reason for audit trails.

---

### Reservations

#### `GET /api/reservations`
- **Auth**: `RequiresPermission("RESERVATION_VIEW")`
- **Description**: Retrieves reservations queue. Scoped to calling student if logged in as student.

#### `POST /api/reservations`
- **Auth**: `RequiresPermission("RESERVATION_CREATE")`
- **Body**: `{ "bookId": 4 }`
- **Description**: Places a reservation hold for a popular book currently checked out.

#### `POST /api/reservations/{id}/cancel`
- **Auth**: `RequiresPermission("RESERVATION_CANCEL")`
- **Description**: Cancels an active reservation. IDOR protected.
