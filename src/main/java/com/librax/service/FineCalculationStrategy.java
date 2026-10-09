package com.librax.service;

import java.math.BigDecimal;

/**
 * Strategy interface for computing an overdue fine. Allows alternate
 * fine schemes to be swapped in without touching LoanService or the UI.
 */
public interface FineCalculationStrategy {
    BigDecimal calculate(long overdueDays, BigDecimal ratePerDay);
}
