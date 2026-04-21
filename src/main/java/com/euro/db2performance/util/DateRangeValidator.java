package com.euro.db2performance.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Validates date ranges for large dataset queries.
 * Enforces maximum query window to prevent performance issues.
 */
@Component
public class DateRangeValidator {

    private static final long MAX_DAYS = 365;

    /**
     * Validates that the date range does not exceed MAX_DAYS
     * @param from start date
     * @param to end date
     * @throws IllegalArgumentException if range exceeds max days or dates are invalid
     */
    public void validate(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Date range cannot be null");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date");
        }

        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > MAX_DAYS) {
            throw new IllegalArgumentException(
                    String.format("Date range cannot exceed %d days. Requested: %d days", MAX_DAYS, daysBetween)
            );
        }
    }
}
