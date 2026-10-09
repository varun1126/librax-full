package com.librax.repository;

import com.librax.model.Fine;
import com.librax.model.Payment;
import java.util.List;
import java.util.Optional;

public interface FineRepository {
    Fine save(Fine fine);
    Optional<Fine> findById(int id);
    List<Fine> findAll();
    List<Fine> findOutstandingByMember(int memberId);
    void update(Fine fine);
    Payment savePayment(Payment payment);
    List<Payment> findPaymentsByFine(int fineId);
}
