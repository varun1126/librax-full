package com.librax.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Fine {
    private Integer id;
    private Integer loanId;
    private Integer memberId;
    private String memberName;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private FineStatus status;
    private LocalDateTime createdAt;

    public Fine() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLoanId() { return loanId; }
    public void setLoanId(Integer loanId) { this.loanId = loanId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public FineStatus getStatus() { return status; }
    public void setStatus(FineStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public BigDecimal getOutstanding() { return amount.subtract(amountPaid); }
}
