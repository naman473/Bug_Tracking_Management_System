package com.startechies.bugtracker.model;

/**
 * Typed summary of bug counts for a project or set of projects.
 * Replaces {@code Map<String, Long>} with stringly-typed keys — field access
 * is compile-time safe (no silent zero on a typo).
 */
public record BugStats(
        long total,
        long open,
        long assigned,
        long closed,
        long critical
) {
    /** Empty stats — used as the starting accumulator and safe default. */
    public static BugStats empty() {
        return new BugStats(0, 0, 0, 0, 0);
    }

    /**
     * Accepts parsed enums — the repository owns the string→enum conversion,
     * keeping BugStats free of SQL string comparison logic.
     * Null status/severity (e.g. unset fields) are treated as non-matching.
     */
    public BugStats accumulate(BugStatus status, Severity severity, long count) {
        return new BugStats(
                total    + count,
                open     + (BugStatus.OPEN     == status   ? count : 0),
                assigned + (BugStatus.ASSIGNED == status   ? count : 0),
                closed   + (BugStatus.CLOSED   == status   ? count : 0),
                critical + (Severity.CRITICAL  == severity ? count : 0)
        );
    }
}
