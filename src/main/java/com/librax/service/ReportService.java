package com.librax.service;

import com.librax.exception.LibraXException;
import com.librax.model.Book;
import com.librax.model.Fine;
import com.librax.model.Loan;
import com.librax.model.Member;
import com.librax.repository.BookRepository;
import com.librax.repository.FineRepository;
import com.librax.repository.LoanRepository;
import com.librax.repository.MemberRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates CSV reports. Called from a SwingWorker for larger datasets
 * so report generation never blocks the EDT (see PHASES.md Phase 13).
 */
public class ReportService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;

    public ReportService(BookRepository bookRepository, MemberRepository memberRepository,
                          LoanRepository loanRepository, FineRepository fineRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.fineRepository = fineRepository;
    }

    public void exportAllBooksCsv(Path target) {
        List<Book> books = bookRepository.findAll();
        writeCsv(target, "ISBN,Title,Author,Category,Publisher,TotalCopies,AvailableCopies",
                books.stream().map(b -> String.join(",",
                        csv(b.getIsbn()), csv(b.getTitle()), csv(b.getAuthorName()), csv(b.getCategoryName()),
                        csv(b.getPublisher()), String.valueOf(b.getTotalCopies()), String.valueOf(b.getAvailableCopies())
                )).toList());
    }

    public void exportOverdueBooksCsv(Path target) {
        List<Loan> overdue = loanRepository.findOverdue();
        writeCsv(target, "LoanID,Book,Member,IssueDate,DueDate",
                overdue.stream().map(l -> String.join(",",
                        String.valueOf(l.getId()), csv(l.getBookTitle()), csv(l.getMemberName()),
                        String.valueOf(l.getIssueDate()), String.valueOf(l.getDueDate())
                )).toList());
    }

    public void exportMembersCsv(Path target) {
        List<Member> members = memberRepository.findAll();
        writeCsv(target, "MembershipNumber,FullName,Email,Phone,Status",
                members.stream().map(m -> String.join(",",
                        csv(m.getMembershipNumber()), csv(m.getFullName()), csv(m.getEmail()),
                        csv(m.getPhone()), String.valueOf(m.getStatus())
                )).toList());
    }

    public void exportFineCollectionCsv(Path target) {
        List<Fine> fines = fineRepository.findAll();
        writeCsv(target, "FineID,Member,Amount,AmountPaid,Status",
                fines.stream().map(f -> String.join(",",
                        String.valueOf(f.getId()), csv(f.getMemberName()), String.valueOf(f.getAmount()),
                        String.valueOf(f.getAmountPaid()), String.valueOf(f.getStatus())
                )).toList());
    }

    public void exportBorrowingHistoryCsv(Path target, int memberId) {
        List<Loan> loans = loanRepository.findAllByMember(memberId);
        writeCsv(target, "LoanID,Book,IssueDate,DueDate,ReturnDate,Status,Fine",
                loans.stream().map(l -> String.join(",",
                        String.valueOf(l.getId()), csv(l.getBookTitle()), String.valueOf(l.getIssueDate()),
                        String.valueOf(l.getDueDate()), String.valueOf(l.getReturnDate()),
                        String.valueOf(l.getStatus()), String.valueOf(l.getFineAmount())
                )).toList());
    }

    private void writeCsv(Path target, String header, List<String> rows) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(target.toFile()))) {
            writer.println(header);
            for (String row : rows) {
                writer.println(row);
            }
        } catch (IOException e) {
            throw new LibraXException("Failed to write CSV report to " + target, e);
        }
    }

    private String csv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return escaped.contains(",") ? "\"" + escaped + "\"" : escaped;
    }
}
