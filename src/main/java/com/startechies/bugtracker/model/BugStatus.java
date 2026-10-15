package com.startechies.bugtracker.model;

/**
 * Canonical bug lifecycle states.  Use these everywhere instead of raw strings.
 * SQL columns store the {@link #dbValue} (e.g. "Open", "Assigned", "Closed").
 */
public enum BugStatus {
    OPEN("Open"),
    ASSIGNED("Assigned"),
    CLOSED("Closed");

    public final String dbValue;

    BugStatus(String dbValue) { this.dbValue = dbValue; }

    /** Parse a DB value; returns {@code null} if unrecognised rather than throwing. */
    public static BugStatus fromDb(String value) {
        if (value == null) return null;
        for (BugStatus s : values()) {
            if (s.dbValue.equalsIgnoreCase(value)) return s;
        }
        return null;
    }
}
