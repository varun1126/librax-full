package com.librax.web;

import com.librax.api.GoogleBooksService;
import com.librax.dto.GoogleBookDto;
import com.librax.model.Author;
import com.librax.model.Book;
import com.librax.model.BookCopy;
import com.librax.model.Category;
import com.librax.security.RequiresPermission;
import com.librax.service.BookService;
import com.librax.web.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@SuppressWarnings("null")
public class BookController {

    private final BookService bookService;
    private final GoogleBooksService googleBooksService;

    public BookController(BookService bookService, GoogleBooksService googleBooksService) {
        this.bookService = bookService;
        this.googleBooksService = googleBooksService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getBooks(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "availableOnly", required = false) Boolean availableOnly,
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        List<Book> all = bookService.search(search);

        if (categoryId != null) {
            all = all.stream()
                    .filter(b -> Objects.equals(b.getCategoryId(), categoryId))
                    .collect(Collectors.toList());
        }

        if (Boolean.TRUE.equals(availableOnly)) {
            all = all.stream()
                    .filter(b -> b.getAvailableCopies() > 0)
                    .collect(Collectors.toList());
        }

        // Sorting
        Comparator<Book> comparator = switch (sortBy.toLowerCase()) {
            case "author" -> Comparator.comparing(b -> Optional.ofNullable(b.getAuthorName()).orElse(""));
            case "newest" -> Comparator.comparing(b -> Optional.ofNullable(b.getCreatedAt()).orElse(java.time.LocalDateTime.MIN), Comparator.reverseOrder());
            case "available" -> Comparator.comparingInt((Book b) -> b.getAvailableCopies()).reversed();
            default -> Comparator.comparing(b -> Optional.ofNullable(b.getTitle()).orElse(""));
        };
        all.sort(comparator);

        int total = all.size();
        int fromIndex = Math.min((page - 1) * limit, total);
        int toIndex = Math.min(fromIndex + limit, total);
        List<Book> paged = all.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("items", paged);
        response.put("books", paged);
        response.put("total", total);
        response.put("page", page);
        response.put("limit", limit);
        response.put("totalPages", (int) Math.ceil((double) total / limit));

        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getBookDetails(@PathVariable("id") int id) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new com.librax.exception.ValidationException("Book not found"));
        List<BookCopy> copies = bookService.getCopies(id);

        Map<String, Object> map = new HashMap<>();
        map.put("book", book);
        map.put("copies", copies);
        return ApiResponse.ok(map);
    }

    @PostMapping
    @RequiresPermission("BOOK_CREATE")
    public ApiResponse<Book> createBook(@RequestBody Book book, @RequestParam(value = "initialCopies", defaultValue = "1") int initialCopies) {
        if (book.getAuthorName() != null && !book.getAuthorName().isBlank()) {
            Author author = bookService.resolveAuthor(book.getAuthorName());
            if (author != null) book.setAuthorId(author.getId());
        }
        if (book.getCategoryName() != null && !book.getCategoryName().isBlank()) {
            Category cat = bookService.resolveCategory(book.getCategoryName());
            if (cat != null) book.setCategoryId(cat.getId());
        }
        Book created = bookService.createBook(book, initialCopies);
        return ApiResponse.ok(created, "Book successfully added to catalog");
    }

    @PutMapping("/{id}")
    @RequiresPermission("BOOK_UPDATE")
    public ApiResponse<Book> updateBook(@PathVariable("id") int id, @RequestBody Book book) {
        book.setId(id);
        if (book.getAuthorName() != null && !book.getAuthorName().isBlank()) {
            Author author = bookService.resolveAuthor(book.getAuthorName());
            if (author != null) book.setAuthorId(author.getId());
        }
        if (book.getCategoryName() != null && !book.getCategoryName().isBlank()) {
            Category cat = bookService.resolveCategory(book.getCategoryName());
            if (cat != null) book.setCategoryId(cat.getId());
        }
        bookService.updateBook(book);
        return ApiResponse.ok(bookService.findById(id).orElse(book), "Book updated successfully");
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("BOOK_DELETE")
    public ApiResponse<Void> deleteBook(@PathVariable("id") int id) {
        bookService.deleteBook(id);
        return ApiResponse.ok(null, "Book removed from catalog");
    }

    @PostMapping("/{id}/copies")
    @RequiresPermission("COPY_CREATE")
    public ApiResponse<BookCopy> addCopy(@PathVariable("id") int id) {
        BookCopy copy = bookService.addCopy(id);
        return ApiResponse.ok(copy, "Physical copy added");
    }

    @DeleteMapping("/{id}/copies/{copyId}")
    @RequiresPermission("COPY_RETIRE")
    public ApiResponse<Void> retireCopy(@PathVariable("id") int id, @PathVariable("copyId") int copyId) {
        bookService.removeCopy(id, copyId);
        return ApiResponse.ok(null, "Copy retired");
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories() {
        return ApiResponse.ok(bookService.getAllCategories());
    }

    @GetMapping("/authors")
    public ApiResponse<List<Author>> getAuthors() {
        return ApiResponse.ok(bookService.getAllAuthors());
    }

    @GetMapping({"/google/search", "/google-search"})
    @RequiresPermission("BOOK_IMPORT")
    public ApiResponse<List<GoogleBookDto>> searchGoogleBooks(@RequestParam("query") String query) {
        return ApiResponse.ok(googleBooksService.search(query, GoogleBooksService.SearchType.KEYWORD));
    }

    @PostMapping({"/google/import", "/google-import"})
    @RequiresPermission("BOOK_IMPORT")
    public ApiResponse<Book> importGoogleBook(@RequestBody GoogleBookDto dto, @RequestParam(value = "copies", defaultValue = "1") int copies) {
        if ((dto.getTitle() == null || dto.getTitle().isBlank()) && dto.getVolumeId() != null) {
            List<GoogleBookDto> results = googleBooksService.search(dto.getVolumeId(), GoogleBooksService.SearchType.KEYWORD);
            if (!results.isEmpty()) {
                dto = results.get(0);
            }
        }

        String rawIsbn = dto.getIsbn();
        String cleanIsbn = null;
        if (rawIsbn != null && com.librax.util.ValidationUtils.isValidIsbn(rawIsbn)) {
            cleanIsbn = rawIsbn.replaceAll("[\\s-]", "").toUpperCase();
        } else {
            long seed = System.currentTimeMillis() + Math.abs((dto.getTitle() != null ? dto.getTitle() : "librax").hashCode());
            cleanIsbn = com.librax.util.ValidationUtils.generateValidIsbn13(seed);
        }

        // If a book with this ISBN already exists, add copies to it instead of failing
        Optional<Book> existing = bookService.findByIsbn(cleanIsbn);
        if (existing.isPresent()) {
            Book existingBook = existing.get();
            int copyCount = Math.max(1, copies);
            for (int i = 0; i < copyCount; i++) {
                bookService.addCopy(existingBook.getId());
            }
            Book updated = bookService.findById(existingBook.getId()).orElse(existingBook);
            return ApiResponse.ok(updated, "Book already exists in catalog. Added " + copyCount + " new copy.");
        }

        Book book = new Book();
        book.setTitle(dto.getTitle() != null && !dto.getTitle().isBlank() ? dto.getTitle().trim() : "Imported Book");
        book.setSubtitle(dto.getSubtitle());
        book.setIsbn(cleanIsbn);
        book.setPublisher(dto.getPublisher() != null ? dto.getPublisher() : "Academic Press");
        book.setDescription(dto.getDescription());
        book.setPageCount(dto.getPageCount() != null ? dto.getPageCount() : 350);
        book.setCoverImageUrl(dto.getThumbnailUrl());

        String authorName = (dto.getAuthorNames() != null && !dto.getAuthorNames().isBlank())
                ? dto.getAuthorNames()
                : (dto.getAuthors() != null && !dto.getAuthors().isBlank() ? dto.getAuthors() : "Academic Author");
        Author author = bookService.resolveAuthor(authorName);
        if (author != null) book.setAuthorId(author.getId());

        String categoryName = (dto.getCategory() != null && !dto.getCategory().isBlank())
                ? dto.getCategory()
                : "General";
        Category cat = bookService.resolveCategory(categoryName);
        if (cat != null) book.setCategoryId(cat.getId());

        Book saved = bookService.createBook(book, Math.max(1, copies));
        return ApiResponse.ok(saved, "Successfully imported into library catalog");
    }
}
