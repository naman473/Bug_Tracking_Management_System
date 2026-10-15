package com.startechies.bugtracker.model;

/**
 * Lifecycle states for a Project.
 */
public enum ProjectState {
    IN_PROGRESS("InProgress"),
    COMPLETED("Completed"),
    ARCHIVED("Archived");

    public final String dbValue;

    ProjectState(String dbValue) { this.dbValue = dbValue; }

    /** Consistent with BugStatus, Severity, and Role — used in ProjectRepository.mapProject. */
    public static ProjectState fromDb(String value) {
        if (value == null) return IN_PROGRESS;
        for (ProjectState s : values()) {
            if (s.dbValue.equals(value)) return s;
        }
        return IN_PROGRESS;
    }
}
