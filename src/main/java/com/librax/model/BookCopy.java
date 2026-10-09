package com.librax.model;

import java.time.LocalDate;

public class BookCopy {
    private Integer id;
    private Integer bookId;
    private String barcode;
    private String location;
    private String shelf;
    private String condition = "GOOD";
    private CopyStatus status = CopyStatus.AVAILABLE;
    private LocalDate acquiredDate;

    public BookCopy() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public CopyStatus getStatus() { return status; }
    public void setStatus(CopyStatus status) { this.status = status; }

    public LocalDate getAcquiredDate() { return acquiredDate; }
    public void setAcquiredDate(LocalDate acquiredDate) { this.acquiredDate = acquiredDate; }
}
