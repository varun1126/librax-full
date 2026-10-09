package com.librax.service;

import com.librax.exception.ValidationException;
import com.librax.logging.AuditLogger;
import com.librax.model.*;
import com.librax.repository.BookRepository;
import com.librax.security.SessionManager;
import com.librax.util.ValidationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business rules for book catalog management: CRUD, search, and copy
 * management. Validation lives here, not in the UI (RULES.md §3).
 */
public class BookService {

    private final BookRepository bookRepository;
    private final AuditLogger auditLogger;
    private final SessionManager sessionManager;

    public BookService(BookRepository bookRepository, AuditLogger auditLogger, SessionManager sessionManager) {
        this.bookRepository = bookRepository;
        this.auditLogger = auditLogger;
        this.sessionManager = sessionManager;
    }

    public Book createBook(Book book, int initialCopies) {
        validate(book);
        if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new ValidationException("A book with this ISBN already exists.");
        }
        book.setTotalCopies(0);
        book.setAvailableCopies(0);
        Book saved = bookRepository.save(book);
        for (int i = 0; i < initialCopies; i++) {
            addCopy(saved.getId());
        }
        currentUserAudit("BOOK_CREATED", "BOOK", saved.getId(), "Created book: " + saved.getTitle());
        return findById(saved.getId()).orElse(saved);
    }

    public void updateBook(Book book) {
        validate(book);
        Optional<Book> existing = bookRepository.findByIsbn(book.getIsbn());
        if (existing.isPresent() && !existing.get().getId().equals(book.getId())) {
            throw new ValidationException("Another book already uses this ISBN.");
        }
        bookRepository.update(book);
        currentUserAudit("BOOK_UPDATED", "BOOK", book.getId(), "Updated book: " + book.getTitle());
    }

    public void deleteBook(int bookId) {
        bookRepository.delete(bookId);
        currentUserAudit("BOOK_DELETED", "BOOK", bookId, "Deleted book #" + bookId);
    }

    public Optional<Book> findById(int id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return ValidationUtils.isBlank(isbn) ? Optional.empty() : bookRepository.findByIsbn(isbn.replaceAll("[\\s-]", "").toUpperCase());
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> search(String query) {
        return ValidationUtils.isBlank(query) ? bookRepository.findAll() : bookRepository.search(query.trim());
    }

    public BookCopy addCopy(int bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ValidationException("Book not found."));
        BookCopy copy = new BookCopy();
        copy.setBookId(bookId);
        copy.setBarcode(book.getIsbn() + "-" + System.nanoTime() % 100000);
        copy.setStatus(CopyStatus.AVAILABLE);
        copy.setAcquiredDate(LocalDate.now());
        BookCopy saved = bookRepository.saveCopy(copy);
        bookRepository.update(withUpdatedCounts(book, +1, +1));
        return saved;
    }

    public void removeCopy(int bookId, int copyId) {
        bookRepository.updateCopyStatus(copyId, CopyStatus.RETIRED);
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ValidationException("Book not found."));
        bookRepository.update(withUpdatedCounts(book, -1, 0));
    }

    public List<BookCopy> getCopies(int bookId) {
        return bookRepository.findCopiesByBook(bookId);
    }

    public List<Author> getAllAuthors() {
        return bookRepository.findAllAuthors();
    }

    public List<Category> getAllCategories() {
        return bookRepository.findAllCategories();
    }

    public Author resolveAuthor(String name) {
        return ValidationUtils.isBlank(name) ? null : bookRepository.findOrCreateAuthor(name.trim());
    }

    public Category resolveCategory(String name) {
        return ValidationUtils.isBlank(name) ? null : bookRepository.findOrCreateCategory(name.trim());
    }

    private Book withUpdatedCounts(Book book, int totalDelta, int availableDelta) {
        book.setTotalCopies(Math.max(0, book.getTotalCopies() + totalDelta));
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + availableDelta));
        return book;
    }

    private void validate(Book book) {
        if (ValidationUtils.isBlank(book.getTitle())) {
            throw new ValidationException("Title is required.");
        }
        if (!ValidationUtils.isValidIsbn(book.getIsbn())) {
            throw new ValidationException("Invalid ISBN. Enter a valid ISBN-10 or ISBN-13.");
        }
    }

    private void currentUserAudit(String action, String entity, Integer entityId, String description) {
        Integer userId = sessionManager.getCurrentUser() != null ? sessionManager.getCurrentUser().getId() : null;
        auditLogger.log(userId, action, entity, entityId, description);
    }
}
