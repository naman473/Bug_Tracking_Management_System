package com.startechies.bugtracker.model;

import java.time.LocalDateTime;

/**
 * Single entry in the bug audit trail.
 * Written whenever a meaningful lifecycle event occurs (assign, mark, close).
 */
public record BugActivity(
        Long          id,
        String        bugId,
        String        actorName,
        String        actionType,   // "ASSIGNED", "MARKED_FOR_CLOSE", "CLOSED", "REPORTED"
        String        detail,       // free-form note (e.g. "Assigned to Alice")
        LocalDateTime occurredAt
) {
    public static BugActivity of(String bugId, String actorName,
                                 String actionType, String detail) {
        return new BugActivity(null, bugId, actorName, actionType, detail, null);
    }
}
