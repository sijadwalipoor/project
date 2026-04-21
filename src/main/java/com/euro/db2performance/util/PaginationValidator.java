package com.euro.db2performance.util;

import org.springframework.stereotype.Component;

/**
 * Validator for pagination parameters.
 */
@Component
public class PaginationValidator {
    
    private static final int MIN_PAGE_NUMBER = 1;
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Validates pagination parameters
     * @param pageNumber the page number to validate (must be >= 1)
     * @param pageSize the page size to validate (must be between 1 and 100)
     * @throws IllegalArgumentException if validation fails
     */
    public void validate(int pageNumber, int pageSize) {
        if (pageNumber < MIN_PAGE_NUMBER) {
            throw new IllegalArgumentException("Page number must be at least " + MIN_PAGE_NUMBER);
        }
        
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE);
        }
    }
}
