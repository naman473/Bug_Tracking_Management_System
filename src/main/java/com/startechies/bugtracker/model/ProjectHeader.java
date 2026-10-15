package com.startechies.bugtracker.model;

import java.time.LocalDate;

/**
 * Projection combining the manager name and start date for a project.
 * Both fields come from the same project+team+users join — one query suffices.
 */
public record ProjectHeader(String managerName, LocalDate startDate) {
    public static ProjectHeader empty() { return new ProjectHeader(null, null); }
}
