package com.example.ecommerce.util;

import com.example.ecommerce.exception.BadRequestException;
import java.util.List;

public class PaginationUtils {
    public static void validate(int page, int size, String sortBy, List<String> allowedFields, String direction) {
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page must be >=0 and size >0");
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Direction must be 'asc' or 'desc'");
        }
        if (!allowedFields.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field. Allowed: " + allowedFields);
        }
    }
}