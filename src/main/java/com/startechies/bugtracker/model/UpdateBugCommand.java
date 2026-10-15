package com.startechies.bugtracker.model;

/**
 * Immutable command for editing an existing bug's title, description, or severity.
 * {@code requestedBy} is the display name of the tester who owns the bug —
 * the repository enforces this matches {@code created_by} so no tester can
 * edit another's bug.
 */
public record UpdateBugCommand(
        String   bugId,
        String   requestedBy,
        String   title,
        String   description,
        Severity severity
) {}
