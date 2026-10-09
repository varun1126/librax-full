-- LibraX SQLite schema. Executed idempotently (IF NOT EXISTS) on every
-- startup by DatabaseManager so re-runs against an existing database are
-- safe no-ops.

CREATE TABLE IF NOT EXISTS roles (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id                     INTEGER PRIMARY KEY AUTOINCREMENT,
    username               TEXT NOT NULL UNIQUE,
    password_hash          TEXT NOT NULL,
    role_id                INTEGER NOT NULL REFERENCES roles(id),
    full_name              TEXT NOT NULL,
    email                  TEXT UNIQUE,
    status                 TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED','LOCKED','DISABLED')),
    is_active              INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    failed_login_attempts  INTEGER NOT NULL DEFAULT 0,
    locked_until           TEXT,
    last_login             TEXT,
    created_at             TEXT NOT NULL,
    updated_at             TEXT
);

CREATE TABLE IF NOT EXISTS departments (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    code TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS courses (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    department_id INTEGER REFERENCES departments(id),
    name          TEXT NOT NULL,
    code          TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS members (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id             INTEGER REFERENCES users(id),
    membership_number   TEXT NOT NULL UNIQUE,
    full_name           TEXT NOT NULL,
    email               TEXT NOT NULL UNIQUE,
    phone               TEXT NOT NULL,
    address             TEXT,
    department_id       INTEGER REFERENCES departments(id),
    course_id           INTEGER REFERENCES courses(id),
    registration_date   TEXT NOT NULL,
    membership_expiry   TEXT NOT NULL,
    status              TEXT NOT NULL CHECK (status IN ('ACTIVE','EXPIRED','SUSPENDED'))
);

CREATE TABLE IF NOT EXISTS authors (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS books (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn               TEXT NOT NULL UNIQUE,
    title              TEXT NOT NULL,
    subtitle           TEXT,
    author_id          INTEGER REFERENCES authors(id),
    publisher          TEXT,
    publication_date   TEXT,
    category_id        INTEGER REFERENCES categories(id),
    language           TEXT,
    description        TEXT,
    page_count         INTEGER CHECK (page_count IS NULL OR page_count >= 0),
    cover_image_url    TEXT,
    shelf_location     TEXT,
    total_copies       INTEGER NOT NULL DEFAULT 0 CHECK (total_copies >= 0),
    available_copies   INTEGER NOT NULL DEFAULT 0 CHECK (available_copies >= 0),
    created_at         TEXT NOT NULL,
    updated_at         TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_books_title    ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_isbn     ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_books_author   ON books(author_id);
CREATE INDEX IF NOT EXISTS idx_books_category ON books(category_id);

CREATE TABLE IF NOT EXISTS book_copies (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id        INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    barcode        TEXT NOT NULL UNIQUE,
    location       TEXT,
    shelf          TEXT,
    condition      TEXT NOT NULL DEFAULT 'GOOD' CHECK (condition IN ('NEW','GOOD','FAIR','POOR')),
    status         TEXT NOT NULL CHECK (status IN ('AVAILABLE','ISSUED','RESERVED','LOST','DAMAGED','RETIRED')),
    acquired_date  TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_copies_book ON book_copies(book_id);

CREATE TABLE IF NOT EXISTS loans (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    book_copy_id  INTEGER NOT NULL REFERENCES book_copies(id),
    member_id     INTEGER NOT NULL REFERENCES members(id),
    issue_date    TEXT NOT NULL,
    due_date      TEXT NOT NULL,
    return_date   TEXT,
    status        TEXT NOT NULL CHECK (status IN ('ACTIVE','RETURNED','OVERDUE','LOST')),
    fine_amount   REAL NOT NULL DEFAULT 0 CHECK (fine_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_loans_member   ON loans(member_id);
CREATE INDEX IF NOT EXISTS idx_loans_status   ON loans(status);
CREATE INDEX IF NOT EXISTS idx_loans_due_date ON loans(due_date);

CREATE TABLE IF NOT EXISTS reservations (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id            INTEGER NOT NULL REFERENCES books(id),
    member_id          INTEGER NOT NULL REFERENCES members(id),
    reservation_date   TEXT NOT NULL,
    status             TEXT NOT NULL CHECK (status IN ('WAITING','READY','FULFILLED','CANCELLED','EXPIRED'))
);

CREATE TABLE IF NOT EXISTS fines (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    loan_id       INTEGER NOT NULL REFERENCES loans(id),
    member_id     INTEGER NOT NULL REFERENCES members(id),
    amount        REAL NOT NULL CHECK (amount >= 0),
    amount_paid   REAL NOT NULL DEFAULT 0 CHECK (amount_paid >= 0),
    status        TEXT NOT NULL CHECK (status IN ('OUTSTANDING','PARTIALLY_PAID','PAID','WAIVED')),
    created_at    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    fine_id       INTEGER NOT NULL REFERENCES fines(id),
    student_id    INTEGER REFERENCES members(id),
    reference     TEXT UNIQUE,
    amount        REAL NOT NULL CHECK (amount > 0),
    method        TEXT NOT NULL CHECK (method IN ('CASH','UPI','CARD','OTHER')),
    paid_at       TEXT NOT NULL,
    recorded_by   INTEGER NOT NULL REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER REFERENCES users(id),
    action       TEXT NOT NULL,
    entity       TEXT NOT NULL,
    entity_id    INTEGER,
    timestamp    TEXT NOT NULL,
    description  TEXT
);

CREATE TABLE IF NOT EXISTS library_settings (
    key   TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS permissions (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL UNIQUE,
    category    TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       TEXT NOT NULL,
    title      TEXT NOT NULL,
    message    TEXT NOT NULL,
    is_read    INTEGER NOT NULL DEFAULT 0 CHECK (is_read IN (0, 1)),
    created_at TEXT NOT NULL,
    read_at    TEXT
);

CREATE TABLE IF NOT EXISTS login_history (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER REFERENCES users(id),
    username       TEXT NOT NULL,
    ip_address     TEXT,
    user_agent     TEXT,
    status         TEXT NOT NULL,
    failure_reason TEXT,
    timestamp      TEXT NOT NULL
);

-- Default roles
INSERT OR IGNORE INTO roles (name) VALUES ('SUPER_ADMIN');
INSERT OR IGNORE INTO roles (name) VALUES ('ADMIN');
INSERT OR IGNORE INTO roles (name) VALUES ('LIBRARIAN');
INSERT OR IGNORE INTO roles (name) VALUES ('STUDENT');

-- Default permissions
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('USER_VIEW', 'USER', 'View system users and profiles');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('USER_CREATE', 'USER', 'Create new staff or admin users');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('USER_UPDATE', 'USER', 'Update user profiles, statuses, and passwords');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('USER_DISABLE', 'USER', 'Disable or suspend user accounts');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('USER_DELETE', 'USER', 'Permanently delete user accounts');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BOOK_VIEW', 'BOOK', 'Search and view library book catalog');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BOOK_CREATE', 'BOOK', 'Create catalog book records');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BOOK_UPDATE', 'BOOK', 'Edit book information and metadata');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BOOK_DELETE', 'BOOK', 'Delete book records from catalog');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BOOK_IMPORT', 'BOOK', 'Import book records from Google Books API');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('COPY_VIEW', 'INVENTORY', 'View physical book copies and barcodes');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('COPY_CREATE', 'INVENTORY', 'Add physical book copies and barcodes');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('COPY_UPDATE', 'INVENTORY', 'Edit copy location, shelf, and condition');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('COPY_RETIRE', 'INVENTORY', 'Retire, mark damaged, or mark lost copies');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('STUDENT_VIEW', 'STUDENT', 'View student accounts and borrowing history');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('STUDENT_CREATE', 'STUDENT', 'Register new student library members');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('STUDENT_UPDATE', 'STUDENT', 'Update student contact and membership details');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('STUDENT_SUSPEND', 'STUDENT', 'Suspend or expire student library membership');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('LOAN_VIEW', 'CIRCULATION', 'View book loans and checkout status');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('LOAN_CREATE', 'CIRCULATION', 'Issue physical books to students');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('LOAN_RETURN', 'CIRCULATION', 'Process returned books and calculate fines');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('LOAN_RENEW', 'CIRCULATION', 'Renew eligible active book loans');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('LOAN_CANCEL', 'CIRCULATION', 'Cancel erroneous or voided loans');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('RESERVATION_VIEW', 'RESERVATION', 'View active book reservations queue');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('RESERVATION_CREATE', 'RESERVATION', 'Reserve unavailable book titles');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('RESERVATION_CANCEL', 'RESERVATION', 'Cancel active reservations');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('RESERVATION_MANAGE', 'RESERVATION', 'Fulfill, promote, or expire reservations');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('FINE_VIEW', 'FINANCE', 'View student fines and penalty records');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('FINE_CREATE', 'FINANCE', 'Assess manual fines or damage fees');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('FINE_WAIVE', 'FINANCE', 'Waive or forgive overdue fines');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('FINE_PAYMENT', 'FINANCE', 'Record fine settlements and payments');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('REPORT_VIEW', 'REPORTS', 'View circulation and inventory reports');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('REPORT_EXPORT', 'REPORTS', 'Export reports to CSV and PDF formats');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('AUDIT_VIEW', 'SECURITY', 'Inspect immutable audit logs and login history');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('SETTINGS_VIEW', 'SETTINGS', 'View library system configuration');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('SETTINGS_UPDATE', 'SETTINGS', 'Modify fine rates and loan policies');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BACKUP_CREATE', 'DATABASE', 'Generate full SQLite database backups');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BACKUP_RESTORE', 'DATABASE', 'Restore database from backup archives');

-- Seed SUPER_ADMIN permissions (All)
INSERT OR IGNORE INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), id FROM permissions;

-- Seed ADMIN permissions (All except BACKUP_RESTORE)
INSERT OR IGNORE INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), id FROM permissions WHERE name != 'BACKUP_RESTORE';

-- Seed LIBRARIAN permissions
INSERT OR IGNORE INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'LIBRARIAN'), id FROM permissions WHERE name IN (
    'BOOK_VIEW', 'BOOK_CREATE', 'BOOK_UPDATE', 'BOOK_IMPORT',
    'COPY_VIEW', 'COPY_CREATE', 'COPY_UPDATE', 'COPY_RETIRE',
    'STUDENT_VIEW', 'STUDENT_CREATE', 'STUDENT_UPDATE',
    'LOAN_VIEW', 'LOAN_CREATE', 'LOAN_RETURN', 'LOAN_RENEW',
    'RESERVATION_VIEW', 'RESERVATION_CREATE', 'RESERVATION_CANCEL', 'RESERVATION_MANAGE',
    'FINE_VIEW', 'FINE_CREATE', 'FINE_PAYMENT',
    'REPORT_VIEW', 'REPORT_EXPORT'
);

-- Seed STUDENT permissions
INSERT OR IGNORE INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = 'STUDENT'), id FROM permissions WHERE name IN (
    'BOOK_VIEW', 'LOAN_VIEW', 'LOAN_RENEW', 'RESERVATION_VIEW', 'RESERVATION_CREATE', 'RESERVATION_CANCEL', 'FINE_VIEW'
);

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('SETTINGS_VIEW', 'SETTINGS', 'View library system configuration');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('SETTINGS_UPDATE', 'SETTINGS', 'Modify fine rates and loan policies');

INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BACKUP_CREATE', 'DATABASE', 'Generate full SQLite database backups');
INSERT OR IGNORE INTO permissions (name, category, description) VALUES ('BACKUP_RESTORE', 'DATABASE', 'Restore database from backup archives');


-- Seed Default Departments and Courses
INSERT OR IGNORE INTO departments (id, name, code) VALUES (1, 'Computer Science & Engineering', 'CSE');
INSERT OR IGNORE INTO departments (id, name, code) VALUES (2, 'Information Technology', 'IT');
INSERT OR IGNORE INTO departments (id, name, code) VALUES (3, 'Electronics & Communication', 'ECE');
INSERT OR IGNORE INTO departments (id, name, code) VALUES (4, 'School of Management', 'SOM');

INSERT OR IGNORE INTO courses (id, department_id, name, code) VALUES (1, 1, 'B.Tech Computer Science', 'CS-BTECH');
INSERT OR IGNORE INTO courses (id, department_id, name, code) VALUES (2, 1, 'M.Tech Artificial Intelligence', 'AI-MTECH');
INSERT OR IGNORE INTO courses (id, department_id, name, code) VALUES (3, 2, 'B.Tech Information Technology', 'IT-BTECH');
INSERT OR IGNORE INTO courses (id, department_id, name, code) VALUES (4, 4, 'Master of Business Administration', 'MBA');

-- Seed Default Library Settings
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('fine_per_day', '5.00');
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('grace_period_days', '1');
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('max_fine_per_loan', '100.00');
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('loan_period_days', '14');
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('max_books_per_member', '5');
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('fine_blocking_issue', 'true');
INSERT OR IGNORE INTO library_settings (key, value) VALUES ('library_name', 'LibraX University Library');
