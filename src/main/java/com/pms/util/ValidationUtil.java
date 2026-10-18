package com.pms.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * All input validation rules in one place.
 * Each method returns an error message, or null when the input is valid.
 */
public class ValidationUtil {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern MOBILE = Pattern.compile("^\\+?[0-9]{10,14}$");
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99"); // DECIMAL(10,2)

    private ValidationUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Validates the registration form. */
    public static String validateUser(String name, String email, String mobile, String password) {
        if (isBlank(name)) {
            return "Name is required.";
        }
        if (name.trim().length() > 100) {
            return "Name must not exceed 100 characters.";
        }
        if (isBlank(email)) {
            return "Email is required.";
        }
        if (email.trim().length() > 150 || !EMAIL.matcher(email.trim()).matches()) {
            return "Please enter a valid email address.";
        }
        if (isBlank(mobile) || !MOBILE.matcher(mobile.trim()).matches()) {
            return "Mobile number must contain 10 to 14 digits.";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }
        return null;
    }

    /** Validates the add-product and edit-product forms. */
    public static String validateProduct(String name, String price, String qty) {
        if (isBlank(name)) {
            return "Product name is required.";
        }
        if (name.trim().length() > 100) {
            return "Product name must not exceed 100 characters.";
        }

        if (isBlank(price)) {
            return "Product price is required.";
        }
        BigDecimal p;
        try {
            p = new BigDecimal(price.trim());
        } catch (NumberFormatException e) {
            return "Price must be a valid number.";
        }
        if (p.signum() < 0) {
            return "Price must not be negative.";
        }
        if (p.scale() > 2) {
            return "Price can have at most 2 decimal places.";
        }
        if (p.compareTo(MAX_PRICE) > 0) {
            return "Price is too large.";
        }

        if (isBlank(qty)) {
            return "Product quantity is required.";
        }
        if (!qty.trim().matches("\\d{1,9}")) {
            return "Quantity must be a non-negative whole number.";
        }
        return null;
    }
}
