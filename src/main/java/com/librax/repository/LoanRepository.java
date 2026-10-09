package com.librax.repository;

import com.librax.model.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(int id);
    List<Loan> findActiveByMember(int memberId);
    List<Loan> findAllByMember(int memberId);
    List<Loan> findAll();
    List<Loan> findOverdue();
    void update(Loan loan);
    long countActiveByMember(int memberId);
}
