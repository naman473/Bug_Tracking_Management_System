package com.startechies.bugtracker.util;

/**
 * Neutral filter-sentinel utility for query parameters.
 * Centralises the "no filter applied" check used across bug-query methods.
 */
public final class FilterUtils {
    private FilterUtils() {}

    /**
     * Returns {@code true} when a filter parameter means "no filter applied".
     * Accepts {@code null}, blank strings, and the conventional sentinel value {@code "all"}.
     */
    public static boolean isUnfiltered(String value) {
        return value == null || value.isBlank() || "all".equalsIgnoreCase(value);
    }
}
