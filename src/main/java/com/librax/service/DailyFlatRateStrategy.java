package com.librax.service;

import java.math.BigDecimal;

/** Flat per-day overdue rate (the default LibraX fine scheme). */
public class DailyFlatRateStrategy implements FineCalculationStrategy {
    @Override
    public BigDecimal calculate(long overdueDays, BigDecimal ratePerDay) {
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        return ratePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }
}
