package com.startechies.bugtracker.model;

/**
 * Typed outcome of {@link com.startechies.bugtracker.service.AuthService#register}.
 * Sealed so every outcome is exhaustive — callers switch on type, no string matching.
 */
public sealed interface RegistrationResult
        permits RegistrationResult.Success, RegistrationResult.Failure {

    /** Registration succeeded. */
    record Success() implements RegistrationResult {}

    /**
     * Registration failed.
     * @param reason human-readable message safe to show in the UI.
     * @param code   machine-readable code for programmatic checks.
     */
    record Failure(String reason, FailureCode code) implements RegistrationResult {}

    enum FailureCode {
        UNKNOWN_ROLE,
        ALREADY_REGISTERED,
        NOT_IN_ALLOWLIST,
        ROLE_MISMATCH,
        DB_ERROR
    }

    // ── Convenience factories ────────────────────────────────────────────────

    static RegistrationResult success() { return new Success(); }

    static RegistrationResult fail(String reason, FailureCode code) {
        return new Failure(reason, code);
    }
}
