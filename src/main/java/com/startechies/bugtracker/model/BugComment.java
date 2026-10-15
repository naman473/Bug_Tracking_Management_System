package com.startechies.bugtracker.model;

import java.time.LocalDateTime;

/**
 * Immutable comment on a bug.
 */
public record BugComment(
        Long          id,
        String        bugId,
        String        authorName,
        String        authorCssClass, // CSS class: "manager" | "tester" | "developer"
        String        body,
        LocalDateTime createdAt
) {
    /** Factory used when inserting (id and createdAt not yet known). */
    public static BugComment of(String bugId, String authorName, String authorCssClass, String body) {
        return new BugComment(null, bugId, authorName, authorCssClass, body, null);
    }

    /** Returns a copy with the generated id and timestamp from the DB insert. */
    public BugComment withIdAndTime(long id, LocalDateTime createdAt) {
        return new BugComment(id, bugId, authorName, authorCssClass, body, createdAt);
    }
}
