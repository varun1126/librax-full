package com.librax.web;

import com.librax.model.Book;
import com.librax.model.Fine;
import com.librax.model.Loan;
import com.librax.repository.BookRepository;
import com.librax.repository.FineRepository;
import com.librax.repository.LoanRepository;
import com.librax.security.RequiresPermission;
import com.librax.web.dto.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@SuppressWarnings("null")
public class ReportController {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;

    public ReportController(BookRepository bookRepository,
                            LoanRepository loanRepository,
                            FineRepository fineRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.fineRepository = fineRepository;
    }

    @GetMapping("/inventory")
    @RequiresPermission("REPORT_VIEW")
    public ApiResponse<Map<String, Object>> getInventoryReport() {
        List<Book> books = bookRepository.findAll();
        int totalBooks = books.size();
        int totalCopies = books.stream().mapToInt(b -> b.getTotalCopies()).sum();
        int availableCopies = books.stream().mapToInt(b -> b.getAvailableCopies()).sum();

        Map<String, Object> map = new HashMap<>();
        map.put("totalBooks", totalBooks);
        map.put("totalCopies", totalCopies);
        map.put("availableCopies", availableCopies);
        map.put("issuedCopies", totalCopies - availableCopies);
        map.put("books", books);
        return ApiResponse.ok(map);
    }

    @GetMapping("/loans")
    @RequiresPermission("REPORT_VIEW")
    public ApiResponse<List<Loan>> getLoansReport() {
        return ApiResponse.ok(loanRepository.findAll());
    }

    @GetMapping("/overdue")
    @RequiresPermission("REPORT_VIEW")
    public ApiResponse<List<Loan>> getOverdueReport() {
        return ApiResponse.ok(loanRepository.findOverdue());
    }

    @GetMapping("/fines")
    @RequiresPermission("REPORT_VIEW")
    public ApiResponse<List<Fine>> getFinesReport() {
        return ApiResponse.ok(fineRepository.findAll());
    }

    @GetMapping("/export")
    @RequiresPermission("REPORT_EXPORT")
    public ResponseEntity<String> exportCsv(@RequestParam String type) {
        StringBuilder csv = new StringBuilder();

        switch (type.toLowerCase()) {
            case "books", "inventory" -> {
                csv.append("ID,ISBN,Title,Author,Category,Total Copies,Available Copies\n");
                for (Book b : bookRepository.findAll()) {
                    csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%d\n",
                            b.getId(),
                            escape(b.getIsbn()),
                            escape(b.getTitle()),
                            escape(b.getAuthorName()),
                            escape(b.getCategoryName()),
                            b.getTotalCopies(),
                            b.getAvailableCopies()));
                }
            }
            case "loans" -> {
                csv.append("Loan ID,Book,Student,Issue Date,Due Date,Return Date,Status,Fine\n");
                for (Loan l : loanRepository.findAll()) {
                    csv.append(String.format("%d,\"%s\",\"%s\",%s,%s,%s,%s,%s\n",
                            l.getId(),
                            escape(l.getBookTitle()),
                            escape(l.getMemberName()),
                            l.getIssueDate(),
                            l.getDueDate(),
                            l.getReturnDate() != null ? l.getReturnDate() : "",
                            l.getStatus(),
                            l.getFineAmount()));
                }
            }
            case "overdue" -> {
                csv.append("Loan ID,Book,Student,Due Date,Fine\n");
                for (Loan l : loanRepository.findOverdue()) {
                    csv.append(String.format("%d,\"%s\",\"%s\",%s,%s\n",
                            l.getId(),
                            escape(l.getBookTitle()),
                            escape(l.getMemberName()),
                            l.getDueDate(),
                            l.getFineAmount()));
                }
            }
            case "fines" -> {
                csv.append("Fine ID,Loan ID,Student ID,Amount,Amount Paid,Outstanding,Status,Date\n");
                for (Fine f : fineRepository.findAll()) {
                    csv.append(String.format("%d,%d,%d,%s,%s,%s,%s,%s\n",
                            f.getId(),
                            f.getLoanId(),
                            f.getMemberId(),
                            f.getAmount(),
                            f.getAmountPaid(),
                            f.getOutstanding(),
                            f.getStatus(),
                            f.getCreatedAt()));
                }
            }
            default -> throw new com.librax.exception.ValidationException("Unknown export type: " + type);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"librax-" + type + "-report.csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.toString());
    }

    private String escape(String s) {
        return s != null ? s.replace("\"", "\"\"") : "";
    }
}
