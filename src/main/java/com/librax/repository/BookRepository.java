package com.librax.repository;

import com.librax.model.Author;
import com.librax.model.Book;
import com.librax.model.BookCopy;
import com.librax.model.Category;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(int id);
    Optional<Book> findByIsbn(String isbn);
    List<Book> findAll();
    List<Book> search(String query);
    void update(Book book);
    void delete(int id);
    void adjustAvailableCopies(int bookId, int delta);

    List<BookCopy> findCopiesByBook(int bookId);
    Optional<BookCopy> findAvailableCopy(int bookId);
    BookCopy saveCopy(BookCopy copy);
    void updateCopyStatus(int copyId, com.librax.model.CopyStatus status);

    List<Author> findAllAuthors();
    Author findOrCreateAuthor(String name);
    List<Category> findAllCategories();
    Category findOrCreateCategory(String name);
}
