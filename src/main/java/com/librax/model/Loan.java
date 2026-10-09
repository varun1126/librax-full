package com.librax.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Loan {
    private Integer id;
    private Integer bookCopyId;
    private Integer memberId;
    private String bookTitle;   // denormalized for display
    private String memberName;  // denormalized for display
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private BigDecimal fineAmount;

    public Loan() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getBookCopyId() { return bookCopyId; }
    public void setBookCopyId(Integer bookCopyId) { this.bookCopyId = bookCopyId; }
    public Integer getCopyId() { return bookCopyId; }
    public void setCopyId(Integer copyId) { this.bookCopyId = copyId; }
    public int getRenewalCount() { return 0; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }
}
