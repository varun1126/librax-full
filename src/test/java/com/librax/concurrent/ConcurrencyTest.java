package com.librax.concurrent;

import com.librax.config.AppConfig;
import com.librax.config.DatabaseConfig;
import com.librax.config.DatabaseManager;
import com.librax.model.Book;
import com.librax.repository.BookRepository;
import com.librax.repository.impl.JdbcBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency test to verify that SQLite Write-Ahead Logging (WAL)
 * and busy timeout settings safely support simultaneous multi-threaded queries
 * without database lock failures (SQLITE_BUSY).
 */
public class ConcurrencyTest {

    private DatabaseManager databaseManager;
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        AppConfig appConfig = AppConfig.getInstance();
        DatabaseConfig dbConfig = new DatabaseConfig(appConfig);
        databaseManager = new DatabaseManager(dbConfig);
        databaseManager.initializeSchema();
        bookRepository = new JdbcBookRepository(databaseManager);
    }

    @Test
    void testConcurrentReadsAndWrites() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successfulReads = new AtomicInteger(0);
        AtomicInteger successfulWrites = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to start at the exact same instant
                    if (index % 4 == 0) {
                        // Write operation
                        Book book = new Book();
                        book.setIsbn("CONC-" + System.nanoTime() + "-" + index);
                        book.setTitle("Concurrent Volume #" + index);
                        book.setTotalCopies(1);
                        book.setAvailableCopies(1);
                        bookRepository.save(book);
                        successfulWrites.incrementAndGet();
                    } else {
                        // Read operation
                        List<Book> books = bookRepository.findAll();
                        assertTrue(books != null);
                        successfulReads.incrementAndGet();
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startLatch.countDown();
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent tasks should complete within timeout");
        assertEquals(0, errors.size(), "Zero concurrency errors or database locks expected. Errors: " + errors);
        assertEquals(threadCount, successfulReads.get() + successfulWrites.get(),
                "All concurrent read and write operations must succeed");
    }
}
