package com.startechies.bugtracker.model;

/**
 * Bug severity levels.
 * {@link #dbValue} is stored in the database (lower-case by convention).
 */
public enum Severity {
    CRITICAL("critical"),
    MAJOR("major"),
    MINOR("minor"),
    TRIVIAL("trivial");

    public final String dbValue;

    Severity(String dbValue) { this.dbValue = dbValue; }

    public static Severity fromDb(String value) {
        if (value == null) return null;
        for (Severity s : values()) {
            if (s.dbValue.equalsIgnoreCase(value)) return s;
        }
        return null;
    }
}
