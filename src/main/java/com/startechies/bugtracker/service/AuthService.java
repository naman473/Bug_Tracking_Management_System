package com.startechies.bugtracker.service;

import com.startechies.bugtracker.model.ChangePasswordResult;
import com.startechies.bugtracker.model.RegistrationResult;
import com.startechies.bugtracker.model.RegistrationResult.FailureCode;
import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.model.User;
import com.startechies.bugtracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns a typed {@link RegistrationResult} — callers switch on the sealed
     * type rather than comparing string literals.
     */
    @Transactional
    public RegistrationResult register(String email, String roleString, String rawPassword) {
        Role requestedRole = Role.fromDb(roleString);
        if (requestedRole == null) {
            return RegistrationResult.fail("Unknown role selected.", FailureCode.UNKNOWN_ROLE);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return RegistrationResult.fail("This email is already registered.", FailureCode.ALREADY_REGISTERED);
        }
        Optional<User> imported = userRepository.findImportedByEmail(email);
        if (imported.isEmpty()) {
            return RegistrationResult.fail(
                    "Registration failed — your email is not in the system.",
                    FailureCode.NOT_IN_ALLOWLIST);
        }
        if (imported.get().getRole() != requestedRole) {
            return RegistrationResult.fail(
                    "The selected role does not match the records for this email.",
                    FailureCode.ROLE_MISMATCH);
        }

        // Static factory makes required fields explicit — no partial-construction risk
        User user = User.forRegistration(
                imported.get().getUserId(),
                imported.get().getUserName(),
                imported.get().getEmailId(),
                imported.get().getRole(),
                passwordEncoder.encode(rawPassword));

        int saved = userRepository.insertRegisteredUser(user);
        return saved > 0
                ? RegistrationResult.success()
                : RegistrationResult.fail("Database error — please try again.", FailureCode.DB_ERROR);
    }

    /**
     * Returns a typed {@link ChangePasswordResult}.
     * Both failure cases produce the same user-facing message to avoid
     * confirming whether a given email address is registered.
     */
    @Transactional
    public ChangePasswordResult changePassword(String email, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ChangePasswordResult.fail(ChangePasswordResult.FailureCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(currentPassword, userOpt.get().getPassword())) {
            return ChangePasswordResult.fail(ChangePasswordResult.FailureCode.WRONG_PASSWORD);
        }
        userRepository.updatePassword(email, passwordEncoder.encode(newPassword));
        return ChangePasswordResult.success();
    }
}
