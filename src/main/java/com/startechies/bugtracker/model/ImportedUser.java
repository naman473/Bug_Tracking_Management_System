package com.startechies.bugtracker.model;

/**
 * Pre-registration allowlist entry. Immutable record.
 * Field names mirror {@link User} to reduce conceptual friction.
 */
public record ImportedUser(
        String userId,
        String userName,
        Role   role,
        String emailId   // matches User.emailId field name
) {}
