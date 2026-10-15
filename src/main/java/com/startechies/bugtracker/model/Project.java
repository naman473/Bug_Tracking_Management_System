package com.startechies.bugtracker.model;

import java.time.LocalDate;

/**
 * Immutable project snapshot. Immutable by default as a Java record.
 * Repositories construct instances; nothing mutates them after creation.
 */
public record Project(
        String       projectId,
        String       projectName,
        String       projectDesc,
        LocalDate    startDate,
        ProjectState projectState
) {
    /** Convenience no-arg factory used when building a new project before ID assignment. */
    public static Project draft(String name, String desc, LocalDate startDate) {
        return new Project(null, name, desc, startDate, ProjectState.IN_PROGRESS);
    }

    /** Returns a copy with the given projectId assigned. */
    public Project withId(String id) {
        return new Project(id, projectName, projectDesc, startDate, projectState);
    }

    /** Returns the DB string of the state (e.g. "InProgress") for template comparisons. */
    public String projectStateValue()  { return projectState != null ? projectState.dbValue : ""; }
}
