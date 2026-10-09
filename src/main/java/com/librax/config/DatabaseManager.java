package com.librax.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.librax.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Owns SQLite connection creation, schema initialization, and additive migrations.
 *
 * <p>Every connection returned by {@link #getConnection()} has
 * {@code PRAGMA foreign_keys = ON} applied, satisfying the requirement
 * that referential integrity is enforced by the database, not just by
 * application code.</p>
 */
public final class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String SCHEMA_RESOURCE = "db/schema.sql";

    private final DatabaseConfig databaseConfig;
    private volatile boolean initialized = false;

    public DatabaseManager(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        try {
            java.nio.file.Files.createDirectories(databaseConfig.getDatabaseDirectory());
        } catch (java.io.IOException e) {
            throw new DatabaseException(
                    "Unable to create application data directory: " + databaseConfig.getDatabaseDirectory(), e);
        }
    }

    /**
     * Opens a new JDBC connection to the SQLite database with foreign-key
     * enforcement enabled. Callers are responsible for closing it
     * (try-with-resources).
     */
    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(databaseConfig.getJdbcUrl());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
                st.execute("PRAGMA busy_timeout = 5000;");
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Unable to open database connection", e);
        }
    }

    /**
     * Creates the data directory (if missing), applies the schema DDL,
     * executes non-destructive migrations, and seeds required baseline users.
     */
    public synchronized void initializeSchema() {
        if (initialized) {
            return;
        }
        String schemaSql = readSchemaResource();
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode = WAL;");
            statement.execute("PRAGMA foreign_keys = OFF;");
            for (String rawStatement : schemaSql.split(";")) {
                String sql = rawStatement.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
            applyMigrations(connection);
            seedInitialData(connection);
            statement.execute("PRAGMA foreign_keys = ON;");
            log.info("Database schema initialized, migrated, and verified at {}", databaseConfig.getDatabaseFile());
            initialized = true;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }

    private void applyMigrations(Connection connection) {
        ensureColumnExists(connection, "users", "status", "TEXT NOT NULL DEFAULT 'ACTIVE'");
        ensureColumnExists(connection, "users", "locked_until", "TEXT");
        ensureColumnExists(connection, "users", "last_login", "TEXT");
        ensureColumnExists(connection, "users", "updated_at", "TEXT");
        ensureColumnExists(connection, "members", "user_id", "INTEGER REFERENCES users(id)");
        ensureColumnExists(connection, "members", "department_id", "INTEGER REFERENCES departments(id)");
        ensureColumnExists(connection, "members", "course_id", "INTEGER REFERENCES courses(id)");
        ensureColumnExists(connection, "book_copies", "shelf", "TEXT");
        ensureColumnExists(connection, "book_copies", "location", "TEXT");
        ensureColumnExists(connection, "book_copies", "condition", "TEXT NOT NULL DEFAULT 'GOOD'");
        ensureColumnExists(connection, "payments", "reference", "TEXT");
        ensureColumnExists(connection, "payments", "student_id", "INTEGER REFERENCES members(id)");
    }

    private void ensureColumnExists(Connection connection, String tableName, String columnName, String columnDefinition) {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            boolean exists = false;
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
                log.info("Applied migration to {}: added column {}", tableName, columnName);
            }
        } catch (SQLException e) {
            log.debug("Migration notice for {}.{}: {}", tableName, columnName, e.getMessage());
        }
    }

    private void seedInitialData(Connection connection) {
        try {
            // Check if initial seeding has already been performed in this database
            boolean alreadySeeded = false;
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT value FROM library_settings WHERE key = 'initial_seed_completed'")) {
                if (rs.next() && "true".equalsIgnoreCase(rs.getString(1))) {
                    alreadySeeded = true;
                }
            }

            int existingBooksCount = 0;
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
                if (rs.next()) existingBooksCount = rs.getInt(1);
            }

            // If the database already has books or was already seeded, preserve all user data
            if (alreadySeeded || existingBooksCount > 0) {
                if (!alreadySeeded) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT OR REPLACE INTO library_settings (key, value) VALUES ('initial_seed_completed', 'true')")) {
                        ps.executeUpdate();
                    }
                }
                log.info("Database contains existing records ({} books). Preserving all changes across sessions.", existingBooksCount);
                return;
            }

            log.info("Fresh database detected. Initializing baseline administrators, student accounts, and catalog...");
            BCrypt.Hasher hasher = BCrypt.withDefaults();

            // 1. Seed Admin
            int adminCount = 0;
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'")) {
                if (rs.next()) adminCount = rs.getInt(1);
            }
            if (adminCount == 0) {
                String adminHash = hasher.hashToString(12, "Admin@123".toCharArray());
                String insertAdmin = """
                    INSERT INTO users (username, password_hash, role_id, full_name, email, status, is_active, created_at)
                    VALUES ('admin', ?, 1, 'System Administrator', 'admin@librax.edu', 'ACTIVE', 1, ?)
                """;
                try (PreparedStatement ps = connection.prepareStatement(insertAdmin)) {
                    ps.setString(1, adminHash);
                    ps.setString(2, LocalDateTime.now().toString());
                    ps.executeUpdate();
                    log.info("Seeded default admin account (admin / Admin@123)");
                }
            }

            // 2. Seed 5 Realistic Students
            String[][] studentData = {
                {"student01", "Harshit Sharma", "harshit@librax.edu", "LIB-CSE-1001", "+91 98765 43210", "Hostel A, Room 302", "1", "1"},
                {"student02", "Priya Nair", "priya.nair@librax.edu", "LIB-CSE-1002", "+91 98765 43211", "Hostel B, Room 104", "1", "1"},
                {"student03", "Rohan Gupta", "rohan.gupta@librax.edu", "LIB-CSE-1003", "+91 98765 43212", "Hostel A, Room 215", "1", "2"},
                {"student04", "Ananya Verma", "ananya.verma@librax.edu", "LIB-CSE-1004", "+91 98765 43213", "Hostel C, Room 408", "2", "3"},
                {"student05", "Vikram Patel", "vikram.patel@librax.edu", "LIB-CSE-1005", "+91 98765 43214", "Day Scholar, Sector 14", "1", "1"}
            };

            for (String[] s : studentData) {
                String username = s[0];
                int exists = 0;
                try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) exists = rs.getInt(1);
                    }
                }
                if (exists == 0) {
                    String hash = hasher.hashToString(12, "Student@123".toCharArray());
                    int userId = 0;
                    String insertUser = """
                        INSERT INTO users (username, password_hash, role_id, full_name, email, status, is_active, created_at)
                        VALUES (?, ?, 4, ?, ?, 'ACTIVE', 1, ?)
                    """;
                    try (PreparedStatement ps = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, username);
                        ps.setString(2, hash);
                        ps.setString(3, s[1]);
                        ps.setString(4, s[2]);
                        ps.setString(5, LocalDateTime.now().toString());
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) userId = keys.getInt(1);
                        }
                    }

                    if (userId > 0) {
                        String insertMember = """
                            INSERT OR IGNORE INTO members (user_id, membership_number, full_name, email, phone, address, department_id, course_id, registration_date, membership_expiry, status)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                        """;
                        try (PreparedStatement ps = connection.prepareStatement(insertMember)) {
                            ps.setInt(1, userId);
                            ps.setString(2, s[3]);
                            ps.setString(3, s[1]);
                            ps.setString(4, s[2]);
                            ps.setString(5, s[4]);
                            ps.setString(6, s[5]);
                            ps.setInt(7, Integer.parseInt(s[6]));
                            ps.setInt(8, Integer.parseInt(s[7]));
                            ps.setString(9, LocalDate.now().toString());
                            ps.setString(10, LocalDate.now().plusYears(1).toString());
                            ps.executeUpdate();
                        }
                    }
                }
            }
            log.info("Verified 5 default college student accounts (student01 - student05 / Student@123)");

            // 3. Seed books only on fresh database
            seedComputerScienceBooks(connection);

            // 4. Mark initial seed completed so it never runs again
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO library_settings (key, value) VALUES ('initial_seed_completed', 'true')")) {
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            log.warn("Notice during baseline data seeding: {}", e.getMessage(), e);
        }
    }

    private void seedComputerScienceBooks(Connection connection) throws SQLException {
        log.info("Seeding pristine collection of 22 Computer Science & Engineering books...");

        // Categories
        String[] categoryNames = {
            "Algorithms & Data Structures",
            "Operating Systems",
            "Computer Networks",
            "Database Systems",
            "Artificial Intelligence & ML",
            "Software Engineering",
            "Computer Architecture",
            "Compilers & Theory",
            "Cybersecurity & Cryptography",
            "Cloud & Distributed Systems",
            "Programming Languages"
        };
        for (String cat : categoryNames) {
            try (PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO categories (name) VALUES (?)")) {
                ps.setString(1, cat);
                ps.executeUpdate();
            }
        }

        // 22 Foundational Computer Science & Engineering Books
        String[][] booksData = {
            {
                "978-0262046305", "Introduction to Algorithms (4th Edition)",
                "Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein",
                "Algorithms & Data Structures", "MIT Press", "2022", "1312",
                "Comprehensive textbook on computer algorithms covering data structures, graph algorithms, dynamic programming, and computational complexity.",
                "https://covers.openlibrary.org/b/isbn/9780262046305-M.jpg", "CS-RACK-01 / Shelf A"
            },
            {
                "978-1119456339", "Operating System Concepts (10th Edition)",
                "Abraham Silberschatz, Peter B. Galvin, Greg Gagne",
                "Operating Systems", "Wiley", "2018", "976",
                "Foundational reference for modern operating systems covering processes, threads, CPU scheduling, memory management, and file systems.",
                "https://covers.openlibrary.org/b/isbn/9781119456339-M.jpg", "CS-RACK-01 / Shelf B"
            },
            {
                "978-0137523658", "Computer Networks (6th Edition)",
                "Andrew S. Tanenbaum, David J. Wetherall",
                "Computer Networks", "Pearson", "2021", "944",
                "Classic guide to network architectures, OSI and TCP/IP protocol stacks, wireless networks, routing algorithms, and network security.",
                "https://covers.openlibrary.org/b/isbn/9780137523658-M.jpg", "CS-RACK-02 / Shelf A"
            },
            {
                "978-0078022159", "Database System Concepts (7th Edition)",
                "Abraham Silberschatz, Henry F. Korth, S. Sudarshan",
                "Database Systems", "McGraw-Hill Education", "2019", "1376",
                "Definitive introduction to relational databases, SQL queries, relational algebra, transaction management, indexing, and distributed data.",
                "https://covers.openlibrary.org/b/isbn/9780078022159-M.jpg", "CS-RACK-02 / Shelf B"
            },
            {
                "978-0134610993", "Artificial Intelligence: A Modern Approach (4th Edition)",
                "Stuart Russell, Peter Norvig",
                "Artificial Intelligence & ML", "Pearson", "2020", "1152",
                "The leading global textbook in artificial intelligence covering intelligent agents, heuristic search, probabilistic reasoning, and deep learning.",
                "https://covers.openlibrary.org/b/isbn/9780134610993-M.jpg", "CS-RACK-03 / Shelf A"
            },
            {
                "978-0262035613", "Deep Learning",
                "Ian Goodfellow, Yoshua Bengio, Aaron Courville",
                "Artificial Intelligence & ML", "MIT Press", "2016", "800",
                "Authoritative resource on neural networks, convolutional networks, recurrent architectures, backpropagation, and generative models.",
                "https://covers.openlibrary.org/b/isbn/9780262035613-M.jpg", "CS-RACK-03 / Shelf B"
            },
            {
                "978-0132350884", "Clean Code: A Handbook of Agile Software Craftsmanship",
                "Robert C. Martin",
                "Software Engineering", "Prentice Hall", "2008", "464",
                "Practitioner guide to writing clean, maintainable, readable code with best practices for refactoring, naming, and unit testing.",
                "https://covers.openlibrary.org/b/isbn/9780132350884-M.jpg", "CS-RACK-04 / Shelf A"
            },
            {
                "978-0201633610", "Design Patterns: Elements of Reusable Object-Oriented Software",
                "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides",
                "Software Engineering", "Addison-Wesley", "1994", "416",
                "The seminal Gang of Four catalog of 23 reusable creational, structural, and behavioral software design patterns.",
                "https://covers.openlibrary.org/b/isbn/9780201633610-M.jpg", "CS-RACK-04 / Shelf B"
            },
            {
                "978-0134092669", "Computer Systems: A Programmer's Perspective (3rd Edition)",
                "Randal E. Bryant, David R. O'Hallaron",
                "Computer Architecture", "Pearson", "2015", "1120",
                "Explains computer architecture, memory hierarchies, processor pipelining, linking, and runtime execution from a programmer viewpoint.",
                "https://covers.openlibrary.org/b/isbn/9780134092669-M.jpg", "CS-RACK-05 / Shelf A"
            },
            {
                "978-0321486813", "Compilers: Principles, Techniques, and Tools (Dragon Book)",
                "Alfred V. Aho, Monica S. Lam, Ravi Sethi, Jeffrey D. Ullman",
                "Compilers & Theory", "Addison-Wesley", "2006", "1040",
                "The premier textbook on compiler construction covering lexical analysis, parsing, syntax-directed translation, and code generation.",
                "https://covers.openlibrary.org/b/isbn/9780321486813-M.jpg", "CS-RACK-05 / Shelf B"
            },
            {
                "978-1133187790", "Introduction to the Theory of Computation (3rd Edition)",
                "Michael Sipser",
                "Compilers & Theory", "Cengage Learning", "2012", "504",
                "Accessible and rigorous study of automata theory, regular expressions, context-free grammars, Turing machines, and NP-completeness.",
                "https://covers.openlibrary.org/b/isbn/9781133187790-M.jpg", "CS-RACK-05 / Shelf C"
            },
            {
                "978-0134101613", "Computer Organization and Architecture (10th Edition)",
                "William Stallings",
                "Computer Architecture", "Pearson", "2015", "864",
                "Explores the structure and design of modern computing hardware, RISC architectures, bus structures, and multi-core processors.",
                "https://covers.openlibrary.org/b/isbn/9780134101613-M.jpg", "CS-RACK-06 / Shelf A"
            },
            {
                "978-1543057386", "Distributed Systems: Principles and Paradigms",
                "Andrew S. Tanenbaum, Maarten van Steen",
                "Cloud & Distributed Systems", "CreateSpace", "2017", "592",
                "Core concepts of distributed computing architectures, RPC, consensus algorithms, replication, and fault-tolerant system design.",
                "https://covers.openlibrary.org/b/isbn/9781543057386-M.jpg", "CS-RACK-06 / Shelf B"
            },
            {
                "978-0134444284", "Cryptography and Network Security: Principles and Practice",
                "William Stallings",
                "Cybersecurity & Cryptography", "Pearson", "2016", "768",
                "Practical foundation in symmetric/asymmetric cryptography, AES, RSA, digital signatures, hashing, and TLS security.",
                "https://covers.openlibrary.org/b/isbn/9780134444284-M.jpg", "CS-RACK-07 / Shelf A"
            },
            {
                "978-0262510875", "Structure and Interpretation of Computer Programs (SICP)",
                "Harold Abelson, Gerald Jay Sussman",
                "Programming Languages", "MIT Press", "1996", "657",
                "Legendary MIT computer science text on procedural abstraction, data abstraction, state manipulation, and interpreters.",
                "https://covers.openlibrary.org/b/isbn/9780262510875-M.jpg", "CS-RACK-07 / Shelf B"
            },
            {
                "978-0135957059", "The Pragmatic Programmer: Your Journey to Mastery",
                "David Thomas, Andrew Hunt",
                "Software Engineering", "Addison-Wesley", "2019", "352",
                "Timeless lessons in developer craftsmanship, software design, refactoring, and engineering productivity.",
                "https://covers.openlibrary.org/b/isbn/9780135957059-M.jpg", "CS-RACK-04 / Shelf C"
            },
            {
                "978-0133591620", "Modern Operating Systems (4th Edition)",
                "Andrew S. Tanenbaum, Herbert Bos",
                "Operating Systems", "Pearson", "2014", "1136",
                "In-depth coverage of contemporary OS design, concurrency, deadlocks, virtualization, and mobile operating systems.",
                "https://covers.openlibrary.org/b/isbn/9780133591620-M.jpg", "CS-RACK-01 / Shelf C"
            },
            {
                "978-1718502703", "Python Crash Course (3rd Edition)",
                "Eric Matthes",
                "Programming Languages", "No Starch Press", "2023", "552",
                "Hands-on, project-based introduction to programming in Python, data analysis, web development, and visualization.",
                "https://covers.openlibrary.org/b/isbn/9781718502703-M.jpg", "CS-RACK-08 / Shelf A"
            },
            {
                "978-1260440232", "Java: The Complete Reference (12th Edition)",
                "Herbert Schildt",
                "Programming Languages", "McGraw-Hill", "2021", "1280",
                "Comprehensive reference covering core Java syntax, OOP, generics, collections, multithreading, and stream APIs.",
                "https://covers.openlibrary.org/b/isbn/9781260440232-M.jpg", "CS-RACK-08 / Shelf B"
            },
            {
                "978-0133387520", "Cloud Computing: Concepts, Technology & Architecture",
                "Thomas Erl, Ricardo Puttini, Zaigham Mahmood",
                "Cloud & Distributed Systems", "Prentice Hall", "2013", "528",
                "Definitive architectural guide to cloud delivery models (IaaS, PaaS, SaaS), cloud security, elasticity, and microservices.",
                "https://covers.openlibrary.org/b/isbn/9780133387520-M.jpg", "CS-RACK-06 / Shelf C"
            },
            {
                "978-0070428072", "Machine Learning",
                "Tom M. Mitchell",
                "Artificial Intelligence & ML", "McGraw-Hill", "1997", "432",
                "Classic foundations of machine learning covering decision trees, neural networks, Bayesian learning, and reinforcement learning.",
                "https://covers.openlibrary.org/b/isbn/9780070428072-M.jpg", "CS-RACK-03 / Shelf C"
            },
            {
                "978-1259872976", "Software Engineering: A Practitioner's Approach (9th Edition)",
                "Roger S. Pressman, Bruce R. Maxim",
                "Software Engineering", "McGraw-Hill Education", "2019", "992",
                "Industry-standard guide to software engineering lifecycle, agile processes, requirements engineering, and quality assurance.",
                "https://covers.openlibrary.org/b/isbn/9781259872976-M.jpg", "CS-RACK-04 / Shelf D"
            }
        };

        String now = LocalDateTime.now().toString();
        int bookIdx = 1;

        for (String[] b : booksData) {
            String isbn = b[0];
            String title = b[1];
            String authorName = b[2];
            String catName = b[3];
            String publisher = b[4];
            String year = b[5];
            int pageCount = Integer.parseInt(b[6]);
            String description = b[7];
            String coverUrl = b[8];
            String shelf = b[9];

            // Resolve / Insert Author
            int authorId = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM authors WHERE name = ?")) {
                ps.setString(1, authorName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) authorId = rs.getInt(1);
                }
            }
            if (authorId == 0) {
                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO authors (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, authorName);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) authorId = keys.getInt(1);
                    }
                }
            }

            // Resolve Category
            int categoryId = 1;
            try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM categories WHERE name = ?")) {
                ps.setString(1, catName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) categoryId = rs.getInt(1);
                }
            }

            // Insert Book (3 copies total, available will be updated based on loans)
            int bookId = 0;
            String insertBook = """
                INSERT INTO books (isbn, title, author_id, publisher, publication_date, category_id, language, description, page_count, cover_image_url, shelf_location, total_copies, available_copies, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, 'English', ?, ?, ?, ?, 3, 3, ?, ?)
            """;
            try (PreparedStatement ps = connection.prepareStatement(insertBook, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, isbn);
                ps.setString(2, title);
                ps.setInt(3, authorId);
                ps.setString(4, publisher);
                ps.setString(5, year.length() == 4 ? year + "-01-01" : year);
                ps.setInt(6, categoryId);
                ps.setString(7, description);
                ps.setInt(8, pageCount);
                ps.setString(9, coverUrl);
                ps.setString(10, shelf);
                ps.setString(11, now);
                ps.setString(12, now);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) bookId = keys.getInt(1);
                }
            }

            // Insert 3 Physical Copies with distinct barcodes
            if (bookId > 0) {
                char[] suffixes = {'A', 'B', 'C'};
                for (char sfx : suffixes) {
                    String barcode = String.format("LIB-CSE-%03d-%c", bookIdx, sfx);
                    String insertCopy = """
                        INSERT INTO book_copies (book_id, barcode, location, shelf, condition, status, acquired_date)
                        VALUES (?, ?, 'Central Library Main Stacks', ?, 'GOOD', 'AVAILABLE', ?)
                    """;
                    try (PreparedStatement ps = connection.prepareStatement(insertCopy)) {
                        ps.setInt(1, bookId);
                        ps.setString(2, barcode);
                        ps.setString(3, shelf);
                        ps.setString(4, LocalDate.now().minusMonths(6).toString());
                        ps.executeUpdate();
                    }
                }
            }
            bookIdx++;
        }

        // Retrieve Members for seeding realistic circulation loans
        int member1 = 0, member2 = 0, member3 = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT m.id FROM members m JOIN users u ON u.id = m.user_id WHERE u.username = 'student01'")) {
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) member1 = rs.getInt(1); }
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT m.id FROM members m JOIN users u ON u.id = m.user_id WHERE u.username = 'student02'")) {
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) member2 = rs.getInt(1); }
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT m.id FROM members m JOIN users u ON u.id = m.user_id WHERE u.username = 'student03'")) {
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) member3 = rs.getInt(1); }
        }

        if (member1 > 0 && member2 > 0) {
            // Seed Active Loan 1: Book 1 (Copy 1) -> Student 1
            seedLoan(connection, 1, member1, LocalDate.now().minusDays(4), LocalDate.now().plusDays(10), null, "ACTIVE", 0.0);
            updateCopyAndBookStatus(connection, 1, "ISSUED");

            // Seed Active Loan 2: Book 2 (Copy 4) -> Student 2
            seedLoan(connection, 4, member2, LocalDate.now().minusDays(2), LocalDate.now().plusDays(12), null, "ACTIVE", 0.0);
            updateCopyAndBookStatus(connection, 4, "ISSUED");

            // Seed Overdue Loan: Book 3 (Copy 7) -> Student 1 (Issued 20 days ago, due 6 days ago -> Overdue with ₹6 fine)
            int overdueLoanId = seedLoan(connection, 7, member1, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6), null, "OVERDUE", 6.0);
            updateCopyAndBookStatus(connection, 7, "ISSUED");

            if (overdueLoanId > 0) {
                String insertFine = """
                    INSERT INTO fines (loan_id, member_id, amount, amount_paid, status, created_at)
                    VALUES (?, ?, 6.00, 0.00, 'OUTSTANDING', ?)
                """;
                try (PreparedStatement ps = connection.prepareStatement(insertFine)) {
                    ps.setInt(1, overdueLoanId);
                    ps.setInt(2, member1);
                    ps.setString(3, LocalDateTime.now().minusDays(5).toString());
                    ps.executeUpdate();
                }
            }

            // Seed Returned Loan: Book 7 (Copy 19) -> Student 3 (Returned 12 days ago)
            if (member3 > 0) {
                seedLoan(connection, 19, member3, LocalDate.now().minusDays(25), LocalDate.now().minusDays(11), LocalDate.now().minusDays(12), "RETURNED", 0.0);
            }
        }

        log.info("Successfully seeded 22 Computer Science books with full physical copies and active/overdue circulation loans!");
    }

    private int seedLoan(Connection connection, int copyId, int memberId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, String status, double fine) throws SQLException {
        String insert = """
            INSERT INTO loans (book_copy_id, member_id, issue_date, due_date, return_date, status, fine_amount)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, copyId);
            ps.setInt(2, memberId);
            ps.setString(3, issueDate.toString());
            ps.setString(4, dueDate.toString());
            ps.setString(5, returnDate != null ? returnDate.toString() : null);
            ps.setString(6, status);
            ps.setDouble(7, fine);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return 0;
    }

    private void updateCopyAndBookStatus(Connection connection, int copyId, String copyStatus) throws SQLException {
        int bookId = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT book_id FROM book_copies WHERE id = ?")) {
            ps.setInt(1, copyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) bookId = rs.getInt(1);
            }
        }
        try (PreparedStatement ps = connection.prepareStatement("UPDATE book_copies SET status = ? WHERE id = ?")) {
            ps.setString(1, copyStatus);
            ps.setInt(2, copyId);
            ps.executeUpdate();
        }
        if (bookId > 0) {
            try (PreparedStatement ps = connection.prepareStatement("UPDATE books SET available_copies = MAX(0, available_copies - 1) WHERE id = ?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }
        }
    }

    private String readSchemaResource() {
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing required classpath resource: " + SCHEMA_RESOURCE);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--")) {
                        sb.append(line).append('\n');
                    }
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new DatabaseException("Failed to read schema resource " + SCHEMA_RESOURCE, e);
        }
    }
}
