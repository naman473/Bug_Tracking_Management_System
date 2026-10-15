package com.startechies.bugtracker.model;

/**
 * Read-only projection of the display fields controllers need.
 * Constructed once per request from a single {@code findByEmail} query.
 */
public record UserDisplay(
        String userName,
        String lastLoginText,
        int    projectCounter
) {}
