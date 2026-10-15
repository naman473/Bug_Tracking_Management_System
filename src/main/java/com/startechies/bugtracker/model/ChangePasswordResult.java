package com.startechies.bugtracker.model;

/**
 * Typed outcome of
 * {@link com.startechies.bugtracker.service.AuthService#changePassword}.
 * Consistent with {@link RegistrationResult} — callers switch on type rather
 * than interpreting an opaque boolean.
 *
 * <p>Both failure cases ({@link FailureCode#USER_NOT_FOUND} and
 * {@link FailureCode#WRONG_PASSWORD}) map to the same user-facing message
 * deliberately — this avoids confirming whether an email exists in the system.
 */
public sealed interface ChangePasswordResult
        permits ChangePasswordResult.Success, ChangePasswordResult.Failure {

    record Success() implements ChangePasswordResult {}
    record Failure(FailureCode code) implements ChangePasswordResult {}

    enum FailureCode {
        /** No registered account found for that email. */
        USER_NOT_FOUND,
        /** Current password did not match the stored hash. */
        WRONG_PASSWORD
    }

    static ChangePasswordResult success()            { return new Success(); }
    static ChangePasswordResult fail(FailureCode c)  { return new Failure(c); }
}
