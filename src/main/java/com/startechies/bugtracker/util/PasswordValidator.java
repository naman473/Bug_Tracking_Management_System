package com.startechies.bugtracker.util;

import java.util.regex.Pattern;

/**
 * Shared password strength validation.
 * Rules:
 *  - At least 8 characters
 *  - At least one letter
 *  - At least one digit
 *  - At least one special character (!@#$%^&*...)
 */
public final class PasswordValidator {
    private PasswordValidator() {}

    private static final Pattern STRONG_PW =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$");

    public static boolean isStrong(String password) {
        return password != null && STRONG_PW.matcher(password).matches();
    }

    public static final String RULE_MESSAGE =
            "Password must be at least 8 characters and contain letters, digits, and a special character.";
}
