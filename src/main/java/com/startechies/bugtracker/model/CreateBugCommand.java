package com.startechies.bugtracker.model;

/**
 * Immutable command object for creating a bug.
 * The repository generates the id, status, and open date — the command
 * carries only the fields the caller provides.
 */
public record CreateBugCommand(
        String   projectName,
        String   createdBy,
        String   title,
        String   description,
        Severity severity
) {}
