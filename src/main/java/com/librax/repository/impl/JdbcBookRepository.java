package com.librax.repository.impl;

import com.librax.config.DatabaseManager;
import com.librax.exception.DatabaseException;
import com.librax.model.*;
import com.librax.repository.BookRepository;
import com.librax.util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookRepository implements BookRepository {

    private final DatabaseManager db;

    public JdbcBookRepository(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_BASE = """
        SELECT b.*, a.name AS author_name, cat.name AS category_name
        FROM books b
        LEFT JOIN authors a ON a.id = b.author_id
        LEFT JOIN categories cat ON cat.id = b.category_id
    """;

    @Override
    public Book save(Book book) {
        String sql = """
            INSERT INTO books (isbn, title, subtitle, author_id, publisher, publication_date,
                                category_id, language, description, page_count, cover_image_url,
                                shelf_location, total_copies, available_copies, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            book.setCreatedAt(now);
            book.setUpdatedAt(now);
            bindBook(ps, book);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) book.setId(keys.getInt(1));
            }
            return book;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save book (duplicate ISBN?)", e);
        }
    }

    private void bindBook(PreparedStatement ps, Book b) throws SQLException {
        ps.setString(1, b.getIsbn());
        ps.setString(2, b.getTitle());
        ps.setString(3, b.getSubtitle());
        if (b.getAuthorId() != null) ps.setInt(4, b.getAuthorId()); else ps.setNull(4, Types.INTEGER);
        ps.setString(5, b.getPublisher());
        ps.setString(6, DateUtils.format(b.getPublicationDate()));
        if (b.getCategoryId() != null) ps.setInt(7, b.getCategoryId()); else ps.setNull(7, Types.INTEGER);
        ps.setString(8, b.getLanguage());
        ps.setString(9, b.getDescription());
        if (b.getPageCount() != null) ps.setInt(10, b.getPageCount()); else ps.setNull(10, Types.INTEGER);
        ps.setString(11, b.getCoverImageUrl());
        ps.setString(12, b.getShelfLocation());
        ps.setInt(13, b.getTotalCopies());
        ps.setInt(14, b.getAvailableCopies());
        ps.setString(15, DateUtils.format(b.getCreatedAt()));
        ps.setString(16, DateUtils.format(b.getUpdatedAt()));
    }

    @Override
    public Optional<Book> findById(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE b.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find book by id", e);
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + " WHERE b.isbn = ?")) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find book by ISBN", e);
        }
    }

    @Override
    public List<Book> findAll() {
        List<Book> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + " ORDER BY b.title")) {
            while (rs.next()) results.add(mapRow(rs));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list books", e);
        }
    }

    @Override
    public List<Book> search(String query) {
        String sql = SELECT_BASE + """
             WHERE b.title LIKE ? OR b.isbn LIKE ? OR a.name LIKE ? OR cat.name LIKE ? OR b.publisher LIKE ?
             ORDER BY b.title
        """;
        String like = "%" + query + "%";
        List<Book> results = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search books", e);
        }
    }

    @Override
    public void update(Book book) {
        String sql = """
            UPDATE books SET isbn=?, title=?, subtitle=?, author_id=?, publisher=?, publication_date=?,
                              category_id=?, language=?, description=?, page_count=?, cover_image_url=?,
                              shelf_location=?, total_copies=?, available_copies=?, updated_at=?
            WHERE id=?
        """;
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            book.setUpdatedAt(LocalDateTime.now());
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getSubtitle());
            if (book.getAuthorId() != null) ps.setInt(4, book.getAuthorId()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, book.getPublisher());
            ps.setString(6, DateUtils.format(book.getPublicationDate()));
            if (book.getCategoryId() != null) ps.setInt(7, book.getCategoryId()); else ps.setNull(7, Types.INTEGER);
            ps.setString(8, book.getLanguage());
            ps.setString(9, book.getDescription());
            if (book.getPageCount() != null) ps.setInt(10, book.getPageCount()); else ps.setNull(10, Types.INTEGER);
            ps.setString(11, book.getCoverImageUrl());
            ps.setString(12, book.getShelfLocation());
            ps.setInt(13, book.getTotalCopies());
            ps.setInt(14, book.getAvailableCopies());
            ps.setString(15, DateUtils.format(book.getUpdatedAt()));
            ps.setInt(16, book.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update book", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete book", e);
        }
    }

    @Override
    public void adjustAvailableCopies(int bookId, int delta) {
        String sql = "UPDATE books SET available_copies = available_copies + ?, updated_at = ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, DateUtils.format(LocalDateTime.now()));
            ps.setInt(3, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to adjust available copies", e);
        }
    }

    @Override
    public List<BookCopy> findCopiesByBook(int bookId) {
        List<BookCopy> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM book_copies WHERE book_id = ? ORDER BY id")) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapCopy(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list copies", e);
        }
    }

    @Override
    public Optional<BookCopy> findAvailableCopy(int bookId) {
        String sql = "SELECT * FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' LIMIT 1";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapCopy(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find available copy", e);
        }
    }

    @Override
    public BookCopy saveCopy(BookCopy copy) {
        String sql = "INSERT INTO book_copies (book_id, barcode, status, acquired_date) VALUES (?, ?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, copy.getBookId());
            ps.setString(2, copy.getBarcode());
            ps.setString(3, copy.getStatus().name());
            ps.setString(4, DateUtils.format(copy.getAcquiredDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) copy.setId(keys.getInt(1));
            }
            return copy;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save book copy", e);
        }
    }

    @Override
    public void updateCopyStatus(int copyId, CopyStatus status) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE book_copies SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, copyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update copy status", e);
        }
    }

    @Override
    public List<Author> findAllAuthors() {
        List<Author> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM authors ORDER BY name")) {
            while (rs.next()) results.add(new Author(rs.getInt("id"), rs.getString("name")));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list authors", e);
        }
    }

    @Override
    public Author findOrCreateAuthor(String name) {
        try (Connection c = db.getConnection()) {
            try (PreparedStatement find = c.prepareStatement("SELECT id FROM authors WHERE name = ?")) {
                find.setString(1, name);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) return new Author(rs.getInt("id"), name);
                }
            }
            try (PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO authors (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, name);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    keys.next();
                    return new Author(keys.getInt(1), name);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find or create author", e);
        }
    }

    @Override
    public List<Category> findAllCategories() {
        List<Category> results = new ArrayList<>();
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM categories ORDER BY name")) {
            while (rs.next()) results.add(new Category(rs.getInt("id"), rs.getString("name")));
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list categories", e);
        }
    }

    @Override
    public Category findOrCreateCategory(String name) {
        try (Connection c = db.getConnection()) {
            try (PreparedStatement find = c.prepareStatement("SELECT id FROM categories WHERE name = ?")) {
                find.setString(1, name);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) return new Category(rs.getInt("id"), name);
                }
            }
            try (PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, name);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    keys.next();
                    return new Category(keys.getInt(1), name);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find or create category", e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setSubtitle(rs.getString("subtitle"));
        int authorId = rs.getInt("author_id");
        if (!rs.wasNull()) b.setAuthorId(authorId);
        b.setAuthorName(rs.getString("author_name"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublicationDate(DateUtils.parseDate(rs.getString("publication_date")));
        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) b.setCategoryId(categoryId);
        b.setCategoryName(rs.getString("category_name"));
        b.setLanguage(rs.getString("language"));
        b.setDescription(rs.getString("description"));
        int pageCount = rs.getInt("page_count");
        if (!rs.wasNull()) b.setPageCount(pageCount);
        b.setCoverImageUrl(rs.getString("cover_image_url"));
        b.setShelfLocation(rs.getString("shelf_location"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setCreatedAt(DateUtils.parseDateTime(rs.getString("created_at")));
        b.setUpdatedAt(DateUtils.parseDateTime(rs.getString("updated_at")));
        return b;
    }

    private BookCopy mapCopy(ResultSet rs) throws SQLException {
        BookCopy copy = new BookCopy();
        copy.setId(rs.getInt("id"));
        copy.setBookId(rs.getInt("book_id"));
        copy.setBarcode(rs.getString("barcode"));
        copy.setStatus(CopyStatus.valueOf(rs.getString("status")));
        copy.setAcquiredDate(DateUtils.parseDate(rs.getString("acquired_date")));
        return copy;
    }
}
